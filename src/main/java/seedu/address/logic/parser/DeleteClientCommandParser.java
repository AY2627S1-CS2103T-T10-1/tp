package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteClientCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed client index for the delete-client command.
 */
public class DeleteClientCommandParser implements Parser<DeleteClientCommand> {

    @Override
    public DeleteClientCommand parse(String args) throws ParseException {
        try {
            Index clientIndex = ParserUtil.parseIndex(args);
            return new DeleteClientCommand(clientIndex);
        } catch (ParseException exception) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClientCommand.MESSAGE_USAGE), exception);
        }
    }
}
