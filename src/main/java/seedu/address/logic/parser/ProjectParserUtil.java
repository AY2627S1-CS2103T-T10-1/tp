package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;

/** Shared parsing helpers for project commands. */
final class ProjectParserUtil {
    private ProjectParserUtil() {}

    /** Parses a positive integer identifier using unsigned ASCII digits only. */
    static Index parseIdentifier(String value, String message) throws ParseException {
        if (!value.matches("[0-9]+")) {
            throw new ParseException(message);
        }
        try {
            int oneBasedValue = Integer.parseInt(value);
            if (oneBasedValue < 1) {
                throw new ParseException(message);
            }
            return Index.fromOneBased(oneBasedValue);
        } catch (NumberFormatException exception) {
            throw new ParseException(message, exception);
        }
    }
}
