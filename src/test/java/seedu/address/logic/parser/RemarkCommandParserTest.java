package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_remark_success() {
        assertParseSuccess(parser, " 1 r/Likes baseball", new RemarkCommand(INDEX_FIRST_PERSON,
                new Remark("Likes baseball")));
    }

    @Test
    public void parse_whitespaceAndPunctuation_success() {
        assertParseSuccess(parser, " \t1\t r/  Call after 6pm; likes 日本語!  ",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Call after 6pm; likes 日本語!")));
    }

    @Test
    public void parse_emptyOrOmittedRemark_removesRemark() {
        for (String input : new String[]{"1", "1 r/", "1 r/   "}) {
            assertParseSuccess(parser, input, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
        }
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String input : new String[]{"", "r/note", "0 r/note", "-1 r/note", "one r/note", "1.5 r/note",
            "2147483648 r/note", "1 2 r/note", "1 unprefixed note"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_duplicateRemark_failure() {
        assertParseFailure(parser, "1 r/first r/second", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_REMARK));
    }
}
