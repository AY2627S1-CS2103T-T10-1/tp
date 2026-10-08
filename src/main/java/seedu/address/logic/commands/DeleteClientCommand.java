package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a client identified by its index in the displayed client list.
 */
public class DeleteClientCommand extends Command {

    public static final String COMMAND_WORD = "delete-client";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes a client from ClientDesk.\n"
            + "Parameters: CLIENT_INDEX (must be a displayed positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Deleted client: %1$s.";
    public static final String MESSAGE_INVALID_INDEX = "The client index provided is invalid.";
    public static final String MESSAGE_SAVE_FAILURE = "Client could not be deleted. No changes were made.";

    private final Index targetIndex;

    /**
     * Creates a command to delete the client at the specified displayed index.
     */
    public DeleteClientCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }

        Person clientToDelete = displayedClients.get(targetIndex.getZeroBased());
        model.deletePerson(clientToDelete);
        return new CommandResult(String.format(MESSAGE_SUCCESS, clientToDelete.getName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DeleteClientCommand otherCommand)) {
            return false;
        }

        return targetIndex.equals(otherCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
