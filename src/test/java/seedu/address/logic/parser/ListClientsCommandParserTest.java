package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListClientsCommand;

public class ListClientsCommandParserTest {

    private final ListClientsCommandParser parser = new ListClientsCommandParser();

    @Test
    public void parse_emptyArgs_returnsListClientsCommand() throws Exception {
        assertTrue(parser.parse("") instanceof ListClientsCommand);
        assertTrue(parser.parse("   ") instanceof ListClientsCommand);
    }

    @Test
    public void parse_extraArgs_throwsParseException() {
        assertParseFailure(parser, "unexpected", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                ListClientsCommand.MESSAGE_USAGE));
    }
}
