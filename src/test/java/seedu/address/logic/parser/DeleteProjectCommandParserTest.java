package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteProjectCommand;

public class DeleteProjectCommandParserTest {
    private final DeleteProjectCommandParser parser = new DeleteProjectCommandParser();

    @Test
    public void parse_positiveIdentifiers_acceptsLeadingZerosAndIntegerBoundary() {
        assertParseSuccess(parser, "1 2", new DeleteProjectCommand(Index.fromOneBased(1), 2));
        assertParseSuccess(parser, "0001 0001", new DeleteProjectCommand(Index.fromOneBased(1), 1));
        assertParseSuccess(parser, "2147483647 2147483647",
                new DeleteProjectCommand(Index.fromOneBased(Integer.MAX_VALUE), Integer.MAX_VALUE));
    }

    @Test
    public void parse_invalidArgumentShapes_reportsUsage() {
        for (String input : new String[] {"", "1", "1 2 extra"}) {
            assertParseFailure(parser, input, DeleteProjectCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_invalidIdentifiers_identifiesTheField() {
        for (String value : new String[] {"x", "0", "000", "-1", "+1", "١", "۱", "１", "2147483648", "2.0"}) {
            assertParseFailure(parser, value + " 1", ParserUtil.MESSAGE_INVALID_INDEX);
            assertParseFailure(parser, "1 " + value, "Project ID must be a positive integer.");
        }
    }
}
