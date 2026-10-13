package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteProjectCommand;

public class DeleteProjectCommandParserTest {
    private final DeleteProjectCommandParser parser = new DeleteProjectCommandParser();

    @Test
    public void parse_twoPositiveIds_returnsDeleteCommand() {
        assertParseSuccess(parser, "1 2", new DeleteProjectCommand(Index.fromOneBased(1), 2));
        assertParseSuccess(parser, "  12   2147483647  ",
                new DeleteProjectCommand(Index.fromOneBased(12), Integer.MAX_VALUE));
    }

    @Test
    public void parse_invalidArgumentShapes_reportsUsage() {
        for (String input : new String[] {"", "1", "1 2 extra", "x 1", "0 1", "-1 1", "1 0", "1 -2",
            "1 +2", "1 2.0", "1 2147483648"}) {
            assertParseFailure(parser, input, DeleteProjectCommand.MESSAGE_USAGE);
        }
    }
}
