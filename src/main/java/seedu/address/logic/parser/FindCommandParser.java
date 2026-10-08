package seedu.address.logic.parser;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonContainsKeywordsPredicate;

/**
 * Parses search keywords and creates a {@code FindCommand}.
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final int MAX_KEYWORD_LENGTH = 50;

    /**
     * Parses whitespace-separated keywords, each containing 1 to 50 characters.
     *
     * @throws ParseException if no keywords are provided or a keyword is invalid.
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_USAGE);
        }

        List<String> keywords = List.of(trimmedArgs.split("\\s+"));
        if (keywords.stream().anyMatch(keyword -> keyword.codePointCount(0, keyword.length()) > MAX_KEYWORD_LENGTH)) {
            throw new ParseException(FindCommand.MESSAGE_USAGE);
        }

        return new FindCommand(new PersonContainsKeywordsPredicate(keywords));
    }

}
