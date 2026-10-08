package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class AddClientCommandIntegrationTest {

    @TempDir
    public Path testFolder;

    @Test
    public void execute_clientCommand_persistsAndReloadsClient() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json"));
        Model model = new ModelManager();
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        CommandResult result = logic.execute("AdD-ClIeNt N/Acme Studio P/+65 9123-4567 E/Bob@acme.com");
        assertEquals("Added client: Acme Studio.", result.getFeedbackToUser());
        Person savedClient = storage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertEquals("Acme Studio", savedClient.getName().fullName);
        assertEquals("6591234567", savedClient.getPhone().value);
        assertEquals("Bob@acme.com", savedClient.getEmail().value);
        assertEquals(AddClientCommand.DEFAULT_ADDRESS, savedClient.getAddress().value);
        assertEquals(savedClient, result.getClientToSelect().orElseThrow());
        assertTrue(savedClient.getTags().isEmpty());
        assertEquals(model.getAddressBook().getPersonList(), storage.readAddressBook().orElseThrow().getPersonList());

        // A formatted duplicate resolves to the same stored phone number.
        assertThrows(CommandException.class, () -> logic.execute(
                "add-client n/Acme Studio p/65-9123-4567 e/other@acme.com"));
        assertThrows(ParseException.class, () -> logic.execute("add-client n/Invalid p/12 e/invalid@acme.com"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());

        // The inherited list command can display the client, so teammates can reuse the same model.
        logic.execute("list");
        assertEquals(savedClient, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_sameNameAndBusinessTags_surviveReload() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json"));
        Model model = new ModelManager();
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        logic.execute("add-client n/O'Neil & Co. (SG) p/81234567 e/one@acme.com t/web-design t/web-design");
        logic.execute("add-client n/O'Neil & Co. (SG) p/87654321 e/two@acme.com t/startup");
        ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(2, reloaded.getPersonList().size());
        assertEquals(1, reloaded.getPersonList().get(0).getTags().size());
        assertEquals(model.getAddressBook().getPersonList(), reloaded.getPersonList());
    }

    @Test
    public void execute_saveFailure_preservesDataFileAndFilteredView() throws Exception {
        Path dataFile = testFolder.resolve("clients.json");
        JsonAddressBookStorage realStorage = new JsonAddressBookStorage(dataFile);
        Model model = new ModelManager();
        Person previous = new PersonBuilder().build();
        model.addPerson(previous);
        model.addPerson(new PersonBuilder().withName("Other").withEmail("other@example.com").build());
        model.updateFilteredPersonList(person -> person.equals(previous));
        realStorage.saveAddressBook(model.getAddressBook());
        String originalFile = Files.readString(dataFile);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook proposed) throws IOException {
                throw new IOException("Simulated save failure");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        assertThrows(CommandException.class, AddClientCommand.MESSAGE_SAVE_FAILURE, () ->
            logic.execute("add-client n/Acme p/91234567 e/bob@acme.com"));
        assertEquals(originalFile, Files.readString(dataFile));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertEquals(List.of(previous), model.getFilteredPersonList());
    }
}
