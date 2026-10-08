package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Displays the details of a client identified by the current list index without changing data.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows the details of the client identified by the displayed list index.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_INVALID_INDEX = "The client index provided is invalid.";

    private final Index targetIndex;

    /**
     * Creates a command for the given index in the currently displayed client list.
     */
    public ViewCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> clients = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= clients.size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        Person client = clients.get(targetIndex.getZeroBased());
        String tags = client.getTags().stream().map(tag -> tag.tagName).sorted()
                .collect(Collectors.joining(", "));
        return new CommandResult("Name: " + client.getName()
                + "\nPhone: " + client.getPhone()
                + "\nEmail: " + client.getEmail()
                + "\nAddress: " + client.getAddress()
                + "\nRemark: " + client.getRemark()
                + "\nTags: " + tags);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ViewCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex));
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(targetIndex.getZeroBased());
    }
}
