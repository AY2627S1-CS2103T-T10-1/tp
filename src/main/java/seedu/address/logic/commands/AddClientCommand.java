package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a client to ClientDesk using the existing person model.
 */
public class AddClientCommand extends Command {

    public static final String COMMAND_WORD = "add-client";
    public static final String MESSAGE_FORMAT = "Usage: add-client n/NAME p/PHONE e/EMAIL [t/TAG]...";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a client to ClientDesk.\n"
            + "Parameters: n/NAME p/PHONE e/EMAIL [t/TAG]...\n"
            + "Example: " + COMMAND_WORD + " n/Acme Studio p/+65 8123 4567 e/hello@acme.sg t/web-design";
    public static final String MESSAGE_SUCCESS = "Added client: %1$s.";
    public static final String MESSAGE_DUPLICATE_CLIENT =
            "This client already exists: %1$s.";
    public static final String MESSAGE_SAVE_FAILURE = "Client could not be saved. No changes were made.";
    public static final String DEFAULT_ADDRESS = "Not provided";

    private final Person toAdd;

    /**
     * Creates an AddClientCommand to add the specified client.
     */
    public AddClientCommand(Person client) {
        toAdd = requireNonNull(client);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_CLIENT, toAdd.getName().fullName));
        }

        model.addPerson(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd.getName().fullName), toAdd);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AddClientCommand otherCommand)) {
            return false;
        }
        return toAdd.equals(otherCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("toAdd", toAdd).toString();
    }
}
