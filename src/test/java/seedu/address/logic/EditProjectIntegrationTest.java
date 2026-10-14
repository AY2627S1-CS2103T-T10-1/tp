package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises edits from command text through atomic saving, reload and existing client commands.
 */
public class EditProjectIntegrationTest {
    private static final Project ORIGINAL = new Project(11, new ProjectTitle("Website redesign"),
            new ProjectDeadline("2026-10-15"),
            ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private JsonAddressBookStorage storage;
    private Logic logic;
    private Person alice;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        alice = ALICE.withProjects(List.of(ORIGINAL), 12);
        model.addPerson(alice);
        model.addPerson(BENSON.withProjects(List.of(ORIGINAL), 12));
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        logic = createLogic(storage);
    }

    @Test
    public void execute_allFields_persistsStableIdAndSelectsUpdatedClient() throws Exception {
        Project updated = new Project(11, new ProjectTitle("Launch"), new ProjectDeadline("2024-02-29"),
                ProjectStatus.COMPLETED, ProjectPriority.HIGH);
        Person expected = alice.withProjects(List.of(updated), 12);
        assertEquals(expected, logic.execute("edit-project 1 11 title/Launch deadline/2024-02-29 "
                + "status/completed priority/high").getClientToSelect().orElseThrow());
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        Model restarted = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        assertEquals(updated, restarted.getFilteredPersonList().getFirst().getProjects().getFirst());
    }

    @Test
    public void execute_filteredClientList_preservesFilterAndOtherClient() throws Exception {
        logic.execute("find Benson");
        logic.execute("edit-project 1 11 priority/low");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(ProjectPriority.LOW,
                model.getFilteredPersonList().getFirst().getProjects().getFirst().getPriority());
        assertEquals(alice, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void execute_saveAndPermissionFailures_preserveLiveDataFilterAndSavedFile() throws Exception {
        storage.saveAddressBook(model.getAddressBook());
        String saved = Files.readString(storage.getAddressBookFilePath());
        logic.execute("find Benson");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        for (IOException exception : List.of(new IOException("Write failed"),
                new AccessDeniedException("clients.json"))) {
            logic = createLogic(failingStorage(exception));
            assertEquals(EditProjectCommand.MESSAGE_SAVE_FAILURE, assertThrows(
                    CommandException.class, () -> logic.execute("edit-project 1 11 status/completed")).getMessage());
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(alice, model.getAddressBook().getPersonList().getFirst());
            assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_unchangedEdit_doesNotSave() throws Exception {
        logic = createLogic(failingStorage(new IOException("Save must not be called")));
        assertEquals("No changes needed for project #11.",
                logic.execute("edit-project 1 11 status/not-started priority/medium").getFeedbackToUser());
        assertEquals(alice, model.getFilteredPersonList().getFirst());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_editAndReload_preserveNextProjectIdCounter() throws Exception {
        model.setPerson(alice, alice.withProjects(alice.getProjects(), 50));
        logic.execute("edit-project 1 11 status/completed");
        assertEquals(50, model.getFilteredPersonList().getFirst().getNextProjectId());
        assertEquals(50, storage.readAddressBook().orElseThrow().getPersonList().getFirst().getNextProjectId());
    }

    @Test
    public void execute_existingContactAndRemarkEdits_preserveProjectsAndPersist() throws Exception {
        for (String command : List.of("edit-client 1 e/new@example.com", "edit 1 p/81234567", "remark 1 r/Retainer")) {
            logic.execute(command);
            assertEquals(List.of(ORIGINAL), model.getFilteredPersonList().getFirst().getProjects());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void execute_view_displaysStableProjectIdAndEditedFields() throws Exception {
        logic.execute("edit-project 1 11 status/completed priority/high");
        String feedback = logic.execute("view 1").getFeedbackToUser();
        assertTrue(feedback.contains("#11 | Website redesign | Deadline: 2026-10-15"
                + " | Status: completed | Priority: high"));
    }

    @Test
    public void execute_reopenAfterReload_preservesOtherValuesAndId() throws Exception {
        logic.execute("edit-project 1 11 status/completed priority/high");
        model = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        logic = createLogic(storage);
        logic.execute("edit-project 1 11 status/in-progress");
        Project expected = new Project(11, ORIGINAL.getTitle(), ORIGINAL.getDeadline(),
                ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);
        assertEquals(expected, model.getFilteredPersonList().getFirst().getProjects().getFirst());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_successfulSave_keepsLiveDataUnchangedUntilSaveReturns() throws Exception {
        JsonAddressBookStorage observedStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook proposedData) throws IOException {
                assertEquals(alice, model.getFilteredPersonList().getFirst());
                assertEquals(ProjectStatus.COMPLETED,
                        proposedData.getPersonList().getFirst().getProjects().getFirst().getStatus());
                super.saveAddressBook(proposedData);
            }
        };
        logic = createLogic(observedStorage);
        logic.execute("edit-project 1 11 status/completed");
        assertEquals(ProjectStatus.COMPLETED,
                model.getFilteredPersonList().getFirst().getProjects().getFirst().getStatus());
    }

    private Logic createLogic(JsonAddressBookStorage clientStorage) {
        return new LogicManager(model, new StorageManager(clientStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private JsonAddressBookStorage failingStorage(IOException failure) {
        return new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw failure;
            }
        };
    }
}
