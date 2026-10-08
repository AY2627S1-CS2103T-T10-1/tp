package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditClientCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.EditClientCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies email editing through parsing, the model, and persistence.
 */
public class EditClientIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        logic = createLogic(addressBookStorage);
    }

    @Test
    public void execute_emailUpdate_preservesOtherFieldsAndPersists() throws Exception {
        String feedback = logic.execute("edit-client 1 e/new@example.com").getFeedbackToUser();
        assertEquals("Updated client: Alice Pauline.", feedback);
        assertEquals(new PersonBuilder(ALICE).withEmail("new@example.com").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(BENSON, model.getFilteredPersonList().get(1));
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().equals(BENSON.getName()));
        logic.execute("edit-client 1 e/new@example.com");
        assertEquals(List.of(new PersonBuilder(BENSON).withEmail("new@example.com").build()),
                model.getFilteredPersonList());
        assertEquals(ALICE, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void execute_hiddenDuplicateEmail_rejectsWithoutChangingData() {
        model.updateFilteredPersonList(person -> person.getName().equals(BENSON.getName()));
        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("edit-client 1 e/ALICE@EXAMPLE.COM"));
        assertEquals("Update would duplicate client: Alice Pauline.", exception.getMessage());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_identicalEmail_doesNotAttemptSave() throws Exception {
        logic = createLogic(failingStorage());
        assertEquals("No changes needed for Alice Pauline.",
                logic.execute("edit-client 1 e/alice@example.com").getFeedbackToUser());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_saveFailure_restoresRecordAndFilter() {
        model.updateFilteredPersonList(person -> person.getName().equals(BENSON.getName()));
        logic = createLogic(failingStorage());
        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("edit-client 1 e/new@example.com"));
        assertEquals(EditClientCommand.MESSAGE_SAVE_FAILURE, exception.getMessage());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_outOfRangeIndex_rejectsWithoutChangingData() {
        CommandException exception = assertThrows(
                CommandException.class, () -> logic.execute("edit-client 999 e/new@example.com"));
        assertEquals("No client at index 999.", exception.getMessage());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void parse_invalidInput_rejectsWithoutChangingData() {
        for (String input : List.of("edit-client", "edit-client 1", "edit-client 0 e/a@b.com",
                "edit-client -1 e/a@b.com", "edit-client +1 e/a@b.com", "edit-client 1.5 e/a@b.com",
                "edit-client 1 e/", "edit-client 1 e/not-an-email", "edit-client 1 e/a@example",
                "edit-client 1 e/a@b.com e/c@d.com", "edit-client 1 e/a@b.com p/91234567",
                "edit-client 1 e/" + "a".repeat(250) + "@b.com")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
        }
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void parse_invalidInput_reportsSpecificErrors() {
        AddressBookParser parser = new AddressBookParser();
        assertEquals(EditClientCommand.MESSAGE_NO_FIELD,
                assertThrows(ParseException.class, () -> parser.parseCommand("edit-client 1")).getMessage());
        assertEquals(EditClientCommand.MESSAGE_INVALID_INDEX, assertThrows(
                ParseException.class, () -> parser.parseCommand("edit-client 0 e/a@b.com")).getMessage());
        assertEquals(EditClientCommandParser.MESSAGE_INVALID_EMAIL, assertThrows(
                ParseException.class, () -> parser.parseCommand("edit-client 1 e/not-an-email")).getMessage());
    }

    /**
     * Uses real JSON storage so successful edits can be read back from disk.
     */
    private Logic createLogic(JsonAddressBookStorage clientStorage) {
        return new LogicManager(model, new StorageManager(clientStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    /**
     * Simulates a write failure to test rollback and ensure unchanged emails skip saving.
     */
    private JsonAddressBookStorage failingStorage() {
        return new JsonAddressBookStorage(temporaryFolder.resolve("unwritable.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated save failure");
            }
        };
    }
}
