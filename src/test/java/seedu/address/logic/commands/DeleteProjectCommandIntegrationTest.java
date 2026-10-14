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
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class DeleteProjectCommandIntegrationTest {
    private static final Project ALPHA_PROJECT = project(1, "Alpha work", "2026-10-15");
    private static final Project ZULU_PROJECT = project(1, "Zulu work", "2026-10-15");

    @TempDir
    public Path testFolder;

    @Test
    public void execute_filteredIndex_persistsDeletionWithoutChangingOtherClient() throws Exception {
        ModelManager model = clientsWithProjects();
        JsonAddressBookStorage storage = storage();
        LogicManager logic = logic(model, storage);
        logic.execute("find zulu");

        CommandResult result = logic.execute("delete-project 1 1");

        assertEquals("Deleted project #1 from Zulu.", result.getFeedbackToUser());
        assertEquals(1, model.getFilteredPersonList().size());
        Person updatedZulu = model.getFilteredPersonList().getFirst();
        assertEquals("Zulu", updatedZulu.getName().fullName);
        assertTrue(updatedZulu.getProjects().isEmpty());
        assertEquals(2, updatedZulu.getNextProjectId());
        assertEquals(List.of(ALPHA_PROJECT), model.getAddressBook().getPersonList().getFirst().getProjects());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restartedLogic = logic(restarted, storage);
        restartedLogic.execute("list-clients");
        assertEquals(2, restarted.getFilteredPersonList().size());
        assertEquals(List.of(ALPHA_PROJECT), restarted.getFilteredPersonList().getFirst().getProjects());
        assertTrue(restarted.getFilteredPersonList().getLast().getProjects().isEmpty());
    }

    @Test
    public void execute_saveFailure_preservesDataFilterFileAndLiveList() throws Exception {
        ModelManager model = clientsWithProjects();
        JsonAddressBookStorage realStorage = storage();
        realStorage.saveAddressBook(model.getAddressBook());
        String beforeFile = Files.readString(realStorage.getAddressBookFilePath());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Zulu"));
        List<Person> beforeView = List.copyOf(model.getFilteredPersonList());
        AtomicInteger listChanges = new AtomicInteger();
        model.getFilteredPersonList().addListener((javafx.collections.ListChangeListener<Person>) change ->
                listChanges.incrementAndGet());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(realStorage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook proposed) throws IOException {
                assertTrue(proposed.getPersonList().getLast().getProjects().isEmpty());
                assertEquals(beforeView, model.getFilteredPersonList());
                throw new AccessDeniedException("Simulated read-only data file");
            }
        };

        CommandException error = assertThrows(CommandException.class, () ->
                logic(model, failingStorage).execute("delete-project 1 1"));

        assertEquals(DeleteProjectCommand.MESSAGE_SAVE_FAILURE, error.getMessage());
        assertEquals(beforeView, model.getFilteredPersonList());
        assertEquals(ZULU_PROJECT, model.getFilteredPersonList().getFirst().getProjects().getFirst());
        assertEquals(2, model.getFilteredPersonList().getFirst().getNextProjectId());
        assertEquals(beforeFile, Files.readString(realStorage.getAddressBookFilePath()));
        assertEquals(0, listChanges.get());
        assertEquals("Deleted project #1 from Zulu.",
                logic(model, realStorage).execute("delete-project 1 1").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().getFirst().getProjects().isEmpty());
    }

    @Test
    public void execute_editClientAndListClients_preserveLinkedProjects() throws Exception {
        ModelManager model = clientsWithProjects();
        JsonAddressBookStorage storage = storage();
        LogicManager logic = logic(model, storage);
        logic.execute("find zulu");

        logic.execute("edit-client 1 e/new-zulu@example.com");
        logic.execute("list-clients");

        Person updatedZulu = model.getAddressBook().getPersonList().getLast();
        assertEquals(List.of(ZULU_PROJECT), updatedZulu.getProjects());
        assertEquals(2, updatedZulu.getNextProjectId());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private ModelManager clientsWithProjects() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alpha").withEmail("alpha@example.com")
                .withProjects(List.of(ALPHA_PROJECT), 2).build());
        model.addPerson(new PersonBuilder().withName("Zulu").withEmail("zulu@example.com")
                .withProjects(List.of(ZULU_PROJECT), 2).build());
        return model;
    }

    private JsonAddressBookStorage storage() {
        return new JsonAddressBookStorage(testFolder.resolve("clients.json"));
    }

    private LogicManager logic(ModelManager model, JsonAddressBookStorage addressBookStorage) {
        return new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    private static Project project(int id, String title, String deadline) {
        return new Project(id, new ProjectTitle(title), new ProjectDeadline(deadline), ProjectStatus.NOT_STARTED,
                ProjectPriority.MEDIUM);
    }
}
