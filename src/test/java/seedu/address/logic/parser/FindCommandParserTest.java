package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.PersonContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "", FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " \n \t \r ", FindCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new PersonContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_keywordLengthBoundaries_returnsFindCommand() {
        assertParseSuccess(parser, "a", new FindCommand(new PersonContainsKeywordsPredicate(List.of("a"))));
        String longestKeyword = "a".repeat(50);
        assertParseSuccess(parser, longestKeyword,
                new FindCommand(new PersonContainsKeywordsPredicate(List.of(longestKeyword))));
    }

    @Test
    public void parse_keywordTooLong_throwsParseException() {
        String invalidKeyword = "a".repeat(51);
        assertParseFailure(parser, invalidKeyword, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "acme " + invalidKeyword, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, invalidKeyword + " acme", FindCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_supplementaryCharacters_countsCodePoints() {
        String supplementaryCharacter = "\uD83D\uDE00";
        String longestKeyword = supplementaryCharacter.repeat(50);
        assertParseSuccess(parser, longestKeyword,
                new FindCommand(new PersonContainsKeywordsPredicate(List.of(longestKeyword))));
        assertParseFailure(parser, supplementaryCharacter.repeat(51), FindCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_punctuationAndDuplicates_preservesKeywords() {
        assertParseSuccess(parser, "hello@acme.com web-design acme acme",
                new FindCommand(new PersonContainsKeywordsPredicate(
                        List.of("hello@acme.com", "web-design", "acme", "acme"))));
    }

}
