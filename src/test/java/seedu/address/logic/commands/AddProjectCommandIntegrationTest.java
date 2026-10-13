package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class AddProjectCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void execute_filteredDisplayedIndex_persistsAndSelectsCorrectClientAcrossRestart() throws Exception {
        ModelManager model = clients();
        JsonAddressBookStorage storage = storage();
        LogicManager logic = logic(model, storage);
        logic.execute("find zulu");
        CommandResult result = logic.execute("AdD-PrOjEcT 1 priority/HIGH title/UI/UX redesign "
                + "status/IN-PROGRESS deadline/9999-12-31");
        Person zulu = model.getFilteredPersonList().getFirst();
        assertEquals("Zulu", zulu.getName().fullName);
        assertEquals(zulu, result.getClientToSelect().orElseThrow());
        assertEquals("Added project #1 to Zulu.", result.getFeedbackToUser());
        assertEquals(ProjectStatus.IN_PROGRESS, zulu.getProjects().getFirst().getStatus());
        assertEquals(ProjectPriority.HIGH, zulu.getProjects().getFirst().getPriority());
        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(model.getAddressBook().getPersonList().get(1).getProjects().isEmpty());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restarted = logic(reloaded, storage);
        restarted.execute("find zulu");
        restarted.execute("add-project 1 title/Second project deadline/9999-12-30");
        assertEquals(List.of(1, 2), reloaded.getFilteredPersonList().getFirst().getProjects().stream()
                .map(project -> project.getId()).toList());
    }

    @Test
    public void execute_saveFailureAndRetry_preservesFileFilterSnapshotAndNextId() throws Exception {
        ModelManager model = clients();
        LogicManager realLogic = logic(model, storage());
        realLogic.execute("find zulu");
        storage().saveAddressBook(model.getAddressBook());
        String beforeFile = Files.readString(storage().getAddressBookFilePath());
        AddressBook beforeData = new AddressBook(model.getAddressBook());
        List<Person> beforeView = List.copyOf(model.getFilteredPersonList());
        AtomicInteger changes = new AtomicInteger();
        model.getFilteredPersonList().addListener((javafx.collections.ListChangeListener<Person>) change ->
                changes.incrementAndGet());
        JsonAddressBookStorage failing = new JsonAddressBookStorage(storage().getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook proposed) throws IOException {
                assertEquals(beforeData, model.getAddressBook());
                assertEquals(beforeView, model.getFilteredPersonList());
                assertEquals(1, proposed.getPersonList().getFirst().getProjects().size());
                throw new AccessDeniedException("Simulated read-only data file");
            }
        };
        CommandException error = assertThrows(CommandException.class, () -> logic(model, failing)
                .execute("add-project 1 title/Website deadline/9999-12-31"));
        assertEquals(AddProjectCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
        assertEquals(beforeData, model.getAddressBook());
        assertEquals(beforeView, model.getFilteredPersonList());
        assertEquals(beforeFile, Files.readString(storage().getAddressBookFilePath()));
        assertEquals(0, changes.get());
        assertEquals(1, model.getFilteredPersonList().getFirst().getNextProjectId());
        assertEquals("Added project #1 to Zulu.", realLogic.execute(
                "add-project 1 title/Website deadline/9999-12-31").getFeedbackToUser());
        assertTrue(beforeData.getPersonList().getFirst().getProjects().isEmpty());
    }

    @Test
    public void execute_badInputAndDuplicate_doNotSaveOrAllocateIds() throws Exception {
        ModelManager model = clients();
        LogicManager logic = logic(model, storage());
        logic.execute("add-project 1 title/Website deadline/9999-12-31");
        String beforeFile = Files.readString(storage().getAddressBookFilePath());
        assertThrows(ParseException.class, () -> logic.execute("add-project 1 title/Bad deadline/2026-02-29"));
        assertThrows(ParseException.class, () -> logic.execute("add-project 1 deadline/9999-12-31 title/Bad\u0000"));
        assertThrows(ParseException.class, () -> logic.execute("add-project 1 deadline/9999-12-31 title/Bad\n"));
        assertThrows(CommandException.class, () -> logic.execute("add-project 9 title/Other deadline/9999-12-31"));
        assertThrows(CommandException.class, () -> logic.execute(
                "add-project 1 title/website deadline/9999-12-31 status/in-progress priority/high"));
        assertEquals(beforeFile, Files.readString(storage().getAddressBookFilePath()));
        assertEquals(2, model.getFilteredPersonList().getFirst().getNextProjectId());
    }

    @Test
    public void execute_clientEditsAndRemarks_retainProjectsAndNextId() throws Exception {
        ModelManager model = clients();
        LogicManager logic = logic(model, storage());
        logic.execute("add-project 1 title/Website deadline/9999-12-31");
        Person initial = model.getFilteredPersonList().getFirst();
        logic.execute("edit-client 1 e/changed@example.com");
        logic.execute("edit 1 n/Aardvark p/91234567 t/retainer");
        logic.execute("remark 1 r/Updated contact");
        Person updated = model.getFilteredPersonList().getFirst();
        assertEquals(initial.getProjects(), updated.getProjects());
        assertEquals(initial.getNextProjectId(), updated.getNextProjectId());
        assertEquals(model.getAddressBook(), storage().readAddressBook().orElseThrow());
        logic.execute("add-project 1 title/Next deadline/9999-12-30");
        assertEquals(2, model.getFilteredPersonList().getFirst().getProjects().get(1).getId());
    }

    @Test
    public void execute_viewOnUnwritableStorage_displaysProjectsWithoutSaving() throws Exception {
        ModelManager model = clients();
        logic(model, storage()).execute("add-project 1 title/Website deadline/9999-12-31");
        JsonAddressBookStorage failing = new JsonAddressBookStorage(storage().getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook proposed) {
                throw new AssertionError("Viewing client details must not save");
            }
        };
        String feedback = logic(model, failing).execute("view 1").getFeedbackToUser();
        assertTrue(feedback.contains("#1 | Website | Deadline: 9999-12-31 | Status: not-started | Priority: medium"));
    }

    @Test
    public void execute_deleteClient_removesOnlyItsAssociatedProjects() throws Exception {
        ModelManager model = clients();
        LogicManager logic = logic(model, storage());
        logic.execute("add-project 1 title/Website deadline/9999-12-31");
        logic.execute("add-project 2 title/Website deadline/9999-12-31");
        logic.execute("delete-client 1");
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals("Zulu", model.getFilteredPersonList().getFirst().getName().fullName);
        assertEquals(1, model.getFilteredPersonList().getFirst().getProjects().size());
        assertEquals(model.getAddressBook(), storage().readAddressBook().orElseThrow());
    }

    private ModelManager clients() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Zulu").withEmail("zulu@example.com").build());
        model.addPerson(new PersonBuilder().withName("Alpha").withEmail("alpha@example.com").build());
        return model;
    }

    private JsonAddressBookStorage storage() {
        return new JsonAddressBookStorage(testFolder.resolve("clients.json"));
    }

    private LogicManager logic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }
}
