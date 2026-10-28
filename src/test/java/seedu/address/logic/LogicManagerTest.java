package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListClientsCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_deleteClient_success() throws Exception {
        model.addPerson(AMY);
        logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("deleteClient.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("deleteClientPrefs.json"))));

        CommandResult result = logic.execute(DeleteClientCommand.COMMAND_WORD + " 1");

        assertEquals(String.format(DeleteClientCommand.MESSAGE_SUCCESS, AMY.getName().fullName),
                result.getFeedbackToUser());
        assertEquals(0, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_deleteClientStorageFailure_keepsClient() {
        model.addPerson(AMY);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("deleteClientFailure.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("deleteClientFailurePrefs.json"))));

        assertCommandFailure(DeleteClientCommand.COMMAND_WORD + " 1", CommandException.class,
                DeleteClientCommand.MESSAGE_SAVE_FAILURE,
                new ModelManager(model.getAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_deleteClientAccessDenied_keepsClient() {
        model.addPerson(AMY);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("deleteClientAccessDenied.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw DUMMY_AD_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("deleteClientAccessDeniedPrefs.json"))));

        assertCommandFailure(DeleteClientCommand.COMMAND_WORD + " 1", CommandException.class,
                DeleteClientCommand.MESSAGE_SAVE_FAILURE,
                new ModelManager(model.getAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_deleteStorageFailure_preservesDataFileAndFilterThroughListAndReload() throws Exception {
        model.addPerson(DANIEL);
        model.addPerson(BENSON);
        Path dataFile = temporaryFolder.resolve("deleteFailure.json");
        SwitchableJsonAddressBookStorage addressBookStorage = new SwitchableJsonAddressBookStorage(dataFile);
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic = createLogic(addressBookStorage);
        logic.execute("find Benson");
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<Person> originalFilter = List.copyOf(model.getFilteredPersonList());
        String originalFile = Files.readString(dataFile);
        addressBookStorage.setFailOnSave(true);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("delete 1"));

        assertEquals(originalData, model.getAddressBook());
        assertEquals(originalFilter, model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(dataFile));
        int saveAttemptsAfterFailure = addressBookStorage.getSaveAttempts();
        addressBookStorage.setFailOnSave(false);
        logic.execute(ListCommand.COMMAND_WORD);
        assertEquals(saveAttemptsAfterFailure, addressBookStorage.getSaveAttempts());
        Model reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(originalData, reloaded.getAddressBook());
    }

    @Test
    public void execute_clearStorageFailure_preservesDataFileAndFilter() throws Exception {
        model.addPerson(DANIEL);
        model.addPerson(BENSON);
        Path dataFile = temporaryFolder.resolve("clearFailure.json");
        SwitchableJsonAddressBookStorage addressBookStorage = new SwitchableJsonAddressBookStorage(dataFile);
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic = createLogic(addressBookStorage);
        logic.execute("find Benson");
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<Person> originalFilter = List.copyOf(model.getFilteredPersonList());
        String originalFile = Files.readString(dataFile);
        addressBookStorage.setFailOnSave(true);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("clear"));

        assertEquals(originalData, model.getAddressBook());
        assertEquals(originalFilter, model.getFilteredPersonList());
        assertEquals(originalFile, Files.readString(dataFile));
        int saveAttemptsAfterFailure = addressBookStorage.getSaveAttempts();
        addressBookStorage.setFailOnSave(false);
        logic.execute(ListCommand.COMMAND_WORD);
        assertEquals(saveAttemptsAfterFailure, addressBookStorage.getSaveAttempts());
        Model reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(originalData, reloaded.getAddressBook());
    }

    @Test
    public void execute_addEditRemarkStorageFailures_preserveDataFileAndFilter() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BENSON);
        Path dataFile = temporaryFolder.resolve("legacyMutationFailure.json");
        SwitchableJsonAddressBookStorage addressBookStorage = new SwitchableJsonAddressBookStorage(dataFile);
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic = createLogic(addressBookStorage);
        logic.execute("find Amy");
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<Person> originalFilter = List.copyOf(model.getFilteredPersonList());
        String originalFile = Files.readString(dataFile);
        addressBookStorage.setFailOnSave(true);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        for (String command : List.of(
                "add n/New Person p/91234567 e/new@example.com a/Somewhere",
                "edit 1 e/updated@example.com",
                "remark 1 r/Updated contact")) {
            assertThrows(CommandException.class, expectedMessage, () -> logic.execute(command));
            assertEquals(originalData, model.getAddressBook());
            assertEquals(originalFilter, model.getFilteredPersonList());
            assertEquals(originalFile, Files.readString(dataFile));
        }
    }

    @Test
    public void execute_successfulLegacyMutations_persist() throws Exception {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("legacyMutationSuccess.json"));
        logic = createLogic(addressBookStorage);
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;

        logic.execute(addCommand);
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
        logic.execute("edit 1 e/updated@example.com");
        assertEquals("updated@example.com", addressBookStorage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getEmail().value);
        logic.execute("remark 1 r/Updated contact");
        assertEquals("Updated contact", addressBookStorage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getRemark().value);
        logic.execute("delete 1");
        assertTrue(addressBookStorage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        logic.execute(addCommand);
        logic.execute("clear");
        assertTrue(addressBookStorage.readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void execute_combiningMarkNames_surviveClientAndLegacyCommandsAndReload() throws Exception {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(
                temporaryFolder.resolve("combiningNames.json"));
        for (String name : List.of("अनिल", "วิชัย", "Jose\u0301")) {
            model = new ModelManager();
            logic = createLogic(addressBookStorage);

            logic.execute("add-client n/" + name + " p/91234567 e/client@example.com");
            assertEquals(name, addressBookStorage.readAddressBook().orElseThrow()
                    .getPersonList().getFirst().getName().fullName);

            logic.execute("edit-client 1 e/updated@example.com");
            assertEquals(name, addressBookStorage.readAddressBook().orElseThrow()
                    .getPersonList().getFirst().getName().fullName);
            assertEquals("updated@example.com", addressBookStorage.readAddressBook().orElseThrow()
                    .getPersonList().getFirst().getEmail().value);

            logic.execute("edit 1 n/" + name + " & Co.");
            assertEquals(name + " & Co.", addressBookStorage.readAddressBook().orElseThrow()
                    .getPersonList().getFirst().getName().fullName);

            logic.execute("add n/" + name + " p/87654321 e/legacy@example.com a/Somewhere");
            ReadOnlyAddressBook reloaded = addressBookStorage.readAddressBook().orElseThrow();
            assertEquals(name, reloaded.getPersonList().getLast().getName().fullName);
            assertEquals(model.getAddressBook(), reloaded);
        }
    }

    @Test
    public void execute_listAndExit_doNotSaveAddressBook() throws Exception {
        SwitchableJsonAddressBookStorage addressBookStorage = new SwitchableJsonAddressBookStorage(
                temporaryFolder.resolve("readOnlyCommands.json"));
        addressBookStorage.setFailOnSave(true);
        logic = createLogic(addressBookStorage);

        logic.execute(ListCommand.COMMAND_WORD);
        CommandResult result = logic.execute(ExitCommand.COMMAND_WORD);

        assertTrue(result.isExit());
        assertEquals(0, addressBookStorage.getSaveAttempts());
        assertFalse(Files.exists(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void execute_listClients_resetsFilterWithoutSaving() throws Exception {
        model.addPerson(DANIEL);
        model.addPerson(BENSON);
        logic.execute("find Benson");

        CommandResult result = logic.execute(ListClientsCommand.COMMAND_WORD);

        assertEquals(ListClientsCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(2, logic.getFilteredPersonList().size());
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_find_doesNotChangeStoredData() throws Exception {
        model.addPerson(DANIEL);
        model.addPerson(BENSON);
        CommandResult result = logic.execute("find MEI friends");
        assertEquals("Found 2 matching clients.", result.getFeedbackToUser());
        assertEquals(List.of(BENSON, DANIEL), logic.getFilteredPersonList());
        assertEquals(List.of(DANIEL, BENSON), model.getAddressBook().getPersonList());
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void execute_invalidFindAfterSearch_preservesDisplayedList() throws Exception {
        model.addPerson(DANIEL);
        model.addPerson(BENSON);
        logic.execute("find Benson");
        for (String invalidCommand : List.of("find", "find " + "a".repeat(51))) {
            assertThrows(ParseException.class, FindCommand.MESSAGE_USAGE, () -> logic.execute(invalidCommand));
            assertEquals(List.of(BENSON), logic.getFilteredPersonList());
        }
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, new ModelManager());
    }

    private Logic createLogic(JsonAddressBookStorage addressBookStorage) {
        return new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("sharedUserPrefs.json"))));
    }

    private static class SwitchableJsonAddressBookStorage extends JsonAddressBookStorage {
        private boolean failOnSave;
        private int saveAttempts;

        SwitchableJsonAddressBookStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            if (failOnSave) {
                throw DUMMY_IO_EXCEPTION;
            }
            super.saveAddressBook(addressBook);
        }

        void setFailOnSave(boolean failOnSave) {
            this.failOnSave = failOnSave;
        }

        int getSaveAttempts() {
            return saveAttempts;
        }
    }
}
