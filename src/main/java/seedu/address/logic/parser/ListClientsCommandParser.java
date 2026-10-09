package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ListClientsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the argument-free list-clients command.
 */
public class ListClientsCommandParser implements Parser<ListClientsCommand> {

    @Override
    public ListClientsCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListClientsCommand.MESSAGE_USAGE));
        }
        return new ListClientsCommand();
    }
}
