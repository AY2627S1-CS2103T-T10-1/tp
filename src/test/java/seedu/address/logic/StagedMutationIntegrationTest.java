package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
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
import seedu.address.testutil.PersonBuilder;

public class StagedMutationIntegrationTest {
    private static final List<MutationCase> MUTATIONS = List.of(
            new MutationCase("add-client n/New p/91234567 e/new@example.com", 3,
                    "Client could not be saved. No changes were made."),
            new MutationCase("edit-client 1 e/updated@example.com", 1,
                    "Client could not be saved. No changes were made."),
            new MutationCase("delete-client 1", 0, "Client could not be deleted. No changes were made."),
            new MutationCase("add-project 1 title/Second deadline/2027-01-01", 1,
                    "Project could not be saved. No changes were made."),
            new MutationCase("edit-project 1 7 status/completed", 1,
                    "Project could not be saved. No changes were made."),
            new MutationCase("delete-project 1 7", 1, "Project could not be deleted. No changes were made."),
            new MutationCase("add n/New p/91234567 e/new@example.com a/Test address", 3, null),
            new MutationCase("edit 1 n/Updated", 2, null),
            new MutationCase("remark 1 r/Updated note", 2, null),
            new MutationCase("delete 1", 0, null),
            new MutationCase("clear", 0, null));

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_mutations_publishOnlyAfterSaveAndPreserveFilterPolicies() throws Exception {
        for (MutationCase mutation : MUTATIONS) {
            Model model = createFilteredModel();
            AddressBook before = new AddressBook(model.getAddressBook());
            ObservingStorage storage = new ObservingStorage(model, before, null);
            Logic logic = new LogicManager(model, storage);

            logic.execute(mutation.input());

            assertEquals(1, storage.saveAttempts, mutation.input());
            assertNotEquals(before, model.getAddressBook(), mutation.input());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow(), mutation.input());
            assertEquals(mutation.visibleClients(), model.getFilteredPersonList().size(), mutation.input());
        }
    }

    @Test
    public void execute_failedMutations_preserveLiveDataFilterAndSpecificErrors() {
        for (IOException failure : List.of(new IOException("unavailable"), new AccessDeniedException("denied"))) {
            for (MutationCase mutation : MUTATIONS) {
                Model model = createFilteredModel();
                AddressBook before = new AddressBook(model.getAddressBook());
                List<Person> displayed = List.copyOf(model.getFilteredPersonList());
                ObservingStorage storage = new ObservingStorage(model, before, failure);
                Logic logic = new LogicManager(model, storage);

                CommandException error = assertThrows(CommandException.class, () -> logic.execute(mutation.input()));

                assertEquals(mutation.failureMessage(failure), error.getMessage(), mutation.input());
                assertSame(failure, error.getCause());
                assertEquals(1, storage.saveAttempts, mutation.input());
                assertEquals(before, model.getAddressBook(), mutation.input());
                assertEquals(displayed, model.getFilteredPersonList(), mutation.input());
            }
        }
    }

    @Test
    public void execute_unchangedEdits_skipSaveOnlyForClientAndProjectCommands() throws Exception {
        for (String input : List.of("edit-client 1 e/zulu@example.com", "edit-project 1 7 status/not-started")) {
            Model model = createFilteredModel();
            AddressBook before = new AddressBook(model.getAddressBook());
            ObservingStorage storage = new ObservingStorage(model, before, new IOException("unavailable"));
            new LogicManager(model, storage).execute(input);
            assertEquals(0, storage.saveAttempts, input);
            assertEquals(before, model.getAddressBook());
            assertEquals(1, model.getFilteredPersonList().size());
        }
        for (String input : List.of("edit 1 e/zulu@example.com", "remark 1 r/")) {
            Model model = createFilteredModel();
            ObservingStorage storage = new ObservingStorage(model, new AddressBook(model.getAddressBook()), null);
            new LogicManager(model, storage).execute(input);
            assertEquals(1, storage.saveAttempts, input);
            assertEquals(2, model.getFilteredPersonList().size());
        }
    }

    private Model createFilteredModel() {
        Project project = new Project(7, new ProjectTitle("Original"), new ProjectDeadline("2027-01-01"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
        Person hidden = new PersonBuilder().withName("Alpha").withEmail("alpha@example.com").build();
        Person visible = new PersonBuilder().withName("Zulu").withEmail("zulu@example.com")
                .withProjects(List.of(project), 8).build();
        Model model = new ModelManager();
        model.addPerson(hidden);
        model.addPerson(visible);
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Zulu"));
        return model;
    }

    private record MutationCase(String input, int visibleClients, String specificFailureMessage) {
        private String failureMessage(IOException failure) {
            if (specificFailureMessage != null) {
                return specificFailureMessage;
            }
            String format = failure instanceof AccessDeniedException
                    ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;
            return String.format(format, failure.getMessage());
        }
    }

    private class ObservingStorage extends StorageManager {
        private final Model liveModel;
        private final AddressBook before;
        private final IOException failure;
        private int saveAttempts;

        ObservingStorage(Model liveModel, AddressBook before, IOException failure) {
            super(new JsonAddressBookStorage(temporaryFolder.resolve("clients.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            this.liveModel = liveModel;
            this.before = before;
            this.failure = failure;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook proposed) throws IOException {
            saveAttempts++;
            assertEquals(before, liveModel.getAddressBook());
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(proposed);
        }
    }
}
