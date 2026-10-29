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
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddProjectCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteProjectCommand;
import seedu.address.logic.commands.EditClientCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
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
        Command command = addressBookParser.parseCommand(commandText);
        logger.info("Executing command: " + command.getClass().getSimpleName());
        if (isMutation(command)) {
            return executeStagedMutation(command);
        }
        return command.execute(model);
    }

    private boolean isMutation(Command command) {
        return command instanceof AddClientCommand
                || command instanceof AddProjectCommand
                || command instanceof DeleteClientCommand
                || command instanceof DeleteProjectCommand
                || command instanceof EditClientCommand
                || command instanceof EditProjectCommand
                || command instanceof AddCommand
                || command instanceof ClearCommand
                || command instanceof DeleteCommand
                || command instanceof EditCommand
                || command instanceof RemarkCommand;
    }

    /**
     * Executes against a snapshot and publishes it only after storage succeeds.
     * The snapshot resolves indexes against the same displayed clients as the live model.
     */
    private CommandResult executeStagedMutation(Command command) throws CommandException {
        Model proposedModel = createProposedModel();
        CommandResult result = command.execute(proposedModel);
        if (canSkipUnchangedSave(command) && model.getAddressBook().equals(proposedModel.getAddressBook())) {
            return result;
        }
        try {
            storage.saveAddressBook(proposedModel.getAddressBook());
        } catch (IOException exception) {
            throw new CommandException(getSaveFailureMessage(command, exception), exception);
        }
        model.setAddressBook(proposedModel.getAddressBook());
        if (shouldShowAllClients(command)) {
            model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        }
        return result;
    }

    private Model createProposedModel() {
        Model proposedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        Set<Person> displayedClients = new HashSet<>(model.getFilteredPersonList());
        proposedModel.updateFilteredPersonList(displayedClients::contains);
        return proposedModel;
    }

    private boolean canSkipUnchangedSave(Command command) {
        return command instanceof EditClientCommand || command instanceof EditProjectCommand;
    }

    private boolean shouldShowAllClients(Command command) {
        return command instanceof AddClientCommand || command instanceof AddCommand
                || command instanceof EditCommand || command instanceof RemarkCommand;
    }

    /**
     * Preserves each command's failure message, including the legacy permission-specific error.
     */
    private String getSaveFailureMessage(Command command, IOException exception) {
        return switch (command) {
            case AddClientCommand ignored -> AddClientCommand.MESSAGE_SAVE_FAILURE;
            case AddProjectCommand ignored -> AddProjectCommand.MESSAGE_SAVE_FAILURE;
            case DeleteClientCommand ignored -> DeleteClientCommand.MESSAGE_SAVE_FAILURE;
            case DeleteProjectCommand ignored -> DeleteProjectCommand.MESSAGE_SAVE_FAILURE;
            case EditClientCommand ignored -> EditClientCommand.MESSAGE_SAVE_FAILURE;
            case EditProjectCommand ignored -> EditProjectCommand.MESSAGE_SAVE_FAILURE;
            default -> String.format(exception instanceof AccessDeniedException
                    ? FILE_OPS_PERMISSION_ERROR_FORMAT : FILE_OPS_ERROR_FORMAT, exception.getMessage());
        };
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
