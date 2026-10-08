package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;

/**
 * Updates a client's email while preserving the rest of the client record.
 * This first increment of edit-client supports only the email field.
 */
public class EditClientCommand extends Command {

    public static final String COMMAND_WORD = "edit-client";
    public static final String MESSAGE_USAGE = "Usage: edit-client CLIENT_INDEX e/EMAIL";
    public static final String MESSAGE_NO_FIELD = "Specify at least one field to update.";
    public static final String MESSAGE_INVALID_INDEX = "Client index must be a displayed positive number.";
    public static final String MESSAGE_SAVE_FAILURE = "Client could not be saved. No changes were made.";

    private final Index targetIndex;
    private final Email email;

    /**
     * Creates an email update for the client at the given displayed index.
     */
    public EditClientCommand(Index targetIndex, Email email) {
        this.targetIndex = requireNonNull(targetIndex);
        this.email = requireNonNull(email);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException("No client at index " + targetIndex.getOneBased() + ".");
        }
        Person client = displayedClients.get(targetIndex.getZeroBased());
        if (client.getEmail().equals(email)) {
            return new CommandResult("No changes needed for " + client.getName() + ".");
        }

        // Check every client, including clients hidden by the current search.
        for (Person other : model.getAddressBook().getPersonList()) {
            if (other != client && other.getEmail().value.equalsIgnoreCase(email.value)) {
                throw new CommandException("Update would duplicate client: " + other.getName() + ".");
            }
        }
        Person updatedClient = new Person(client.getName(), client.getPhone(), email,
                client.getAddress(), client.getRemark(), client.getTags());
        model.setPerson(client, updatedClient);
        return new CommandResult("Updated client: " + updatedClient.getName() + ".");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof EditClientCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex) && email.equals(otherCommand.email));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex.getZeroBased(), email);
    }
}
