package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.ClientDeskHelp;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class HelpIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void execute_helpWithUnwritableStorage_preservesRecordsFileAndFilter() throws Exception {
        ModelManager model = createModel();
        model.updateFilteredPersonList(client -> client.getName().fullName.equals("Client 12"));
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<?> originalDisplay = List.copyOf(model.getFilteredPersonList());
        Path dataFile = testFolder.resolve("clients.json");
        new JsonAddressBookStorage(dataFile).saveAddressBook(model.getAddressBook());
        byte[] originalBytes = Files.readAllBytes(dataFile);
        AtomicInteger saveAttempts = new AtomicInteger();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                saveAttempts.incrementAndGet();
                throw new IOException("Storage is read-only");
            }
        };
        LogicManager logic = createLogic(model, storage);

        assertEquals(ClientDeskHelp.getOverview(), logic.execute("help").getFeedbackToUser());
        for (String name : commandNames()) {
            CommandResult result = logic.execute("HeLp " + name.toUpperCase(Locale.ROOT));
            assertEquals(ClientDeskHelp.getDetails(name), result.getFeedbackToUser());
            assertTrue(result.isShowHelp());
            assertFalse(result.isExit());
        }
        assertThrows(ParseException.class, () -> logic.execute("help unknown"));
        assertThrows(ParseException.class, HelpCommand.MESSAGE_USAGE, () -> logic.execute("help add extra"));
        assertEquals(0, saveAttempts.get());
        assertEquals(originalData, model.getAddressBook());
        assertEquals(originalDisplay, model.getFilteredPersonList());
        assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
    }

    @TestFactory
    public Stream<DynamicTest> execute_helpExamples_validAgainstDisposableData() {
        return commandNames().stream().flatMap(name -> {
            String examples = ClientDeskHelp.getDetails(name).split("\nExamples: ", 2)[1];
            return Stream.of(examples.split("; ")).map(example -> DynamicTest.dynamicTest(example, () -> {
                ModelManager model = createModel();
                JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("example.json"));
                storage.saveAddressBook(model.getAddressBook());
                CommandResult result = createLogic(model, storage).execute(example);
                assertFalse(result.getFeedbackToUser().isBlank());
                assertEquals(example.startsWith("help"), result.isShowHelp());
                assertEquals(example.equals("exit"), result.isExit());
                assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            }));
        });
    }

    private LogicManager createLogic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    private List<String> commandNames() {
        return ClientDeskHelp.getOverview().lines().filter(line -> line.contains(" - "))
                .map(line -> line.split(" - ", 2)[0]).toList();
    }

    private ModelManager createModel() {
        ModelManager model = new ModelManager();
        Project project = new Project(2, new ProjectTitle("Existing work"), new ProjectDeadline("2026-12-01"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
        for (int i = 1; i <= 12; i++) {
            model.addPerson(new PersonBuilder().withName(String.format(Locale.ROOT, "Client %02d", i))
                    .withEmail("client" + i + "@example.com").withProjects(List.of(project), 3).build());
        }
        return model;
    }
}
