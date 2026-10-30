package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteClientCommand;

public class DeleteClientCommandParserTest {

    private final DeleteClientCommandParser parser = new DeleteClientCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteClientCommand() {
        assertParseSuccess(parser, "1", new DeleteClientCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "0001", new DeleteClientCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteClientCommand.MESSAGE_USAGE);
        for (String input : new String[] {"invalid", "0", "-1", "+1", "1.0", "١", "１", "2147483648"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }
}
