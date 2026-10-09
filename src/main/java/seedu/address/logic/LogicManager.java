package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddClientCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.commands.EditClientCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListClientsCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof AddClientCommand) {
            return executeAddClient(command);
        }

        if (command instanceof DeleteClientCommand deleteClientCommand) {
            return executeDeleteClient(deleteClientCommand);
        }

        if (command instanceof EditClientCommand editClientCommand) {
            return executeEditClient(editClientCommand);
        }
        commandResult = command.execute(model);
        if (command instanceof FindCommand) {
            // Avoid saving unchanged data or reporting a save error for a read-only search.
            return commandResult;
        }

        if (command instanceof HelpCommand || command instanceof ListClientsCommand) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    /**
     * Saves the proposed client data before publishing it to the live model and UI.
     */
    private CommandResult executeAddClient(Command command) throws CommandException {
        Model proposedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        CommandResult result = command.execute(proposedModel);
        try {
            storage.saveAddressBook(proposedModel.getAddressBook());
        } catch (IOException error) {
            throw new CommandException(AddClientCommand.MESSAGE_SAVE_FAILURE, error);
        }
        model.setAddressBook(proposedModel.getAddressBook());
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        return result;
    }

    /**
     * Saves a proposed client deletion before publishing it to the live model.
     */
    private CommandResult executeDeleteClient(DeleteClientCommand command) throws CommandException {
        Model proposedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        Set<Person> displayedClients = new HashSet<>(model.getFilteredPersonList());
        proposedModel.updateFilteredPersonList(displayedClients::contains);
        CommandResult result = command.execute(proposedModel);

        try {
            storage.saveAddressBook(proposedModel.getAddressBook());
        } catch (IOException exception) {
            throw new CommandException(DeleteClientCommand.MESSAGE_SAVE_FAILURE, exception);
        }

        model.setAddressBook(proposedModel.getAddressBook());
        return result;
    }

    /**
     * Saves an email update before returning success and restores the record if saving fails.
     * Unchanged emails do not trigger a save. The current list filter is preserved.
     */
    private CommandResult executeEditClient(EditClientCommand command) throws CommandException {
        AddressBook previousData = new AddressBook(model.getAddressBook());
        CommandResult result = command.execute(model);
        if (previousData.equals(model.getAddressBook())) {
            return result;
        }
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            model.setAddressBook(previousData);
            throw new CommandException(EditClientCommand.MESSAGE_SAVE_FAILURE, ioe);
        }
        return result;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
