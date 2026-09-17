package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_indexAndRemarkSpecified_success() {
        String remark = "Likes to swim.";
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK + remark;

        assertParseSuccess(parser, userInput,
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark(remark)));
    }

    @Test
    public void parse_emptyRemark_success() {
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK;

        assertParseSuccess(parser, userInput,
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "a " + PREFIX_REMARK + "Likes to swim.",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
    }
}
