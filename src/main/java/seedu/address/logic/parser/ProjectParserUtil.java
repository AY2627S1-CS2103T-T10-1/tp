package seedu.address.logic.parser;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;

/** Shared parsing helpers for project commands. */
final class ProjectParserUtil {
    private static final Pattern PARAMETER_PREFIX = Pattern.compile(
            "(?<!\\S)(title/|deadline/|status/|priority/)", Pattern.CASE_INSENSITIVE);

    private ProjectParserUtil() {}

    /**
     * Extracts case-insensitive prefixes while preserving control characters inside field values.
     * Supported prefixes delimit fields; other slash-containing words remain part of the current value.
     */
    static ArgumentMultimap tokenize(String args) {
        ArgumentMultimap arguments = new ArgumentMultimap();
        Matcher matcher = PARAMETER_PREFIX.matcher(args);
        Prefix previousPrefix = new Prefix("");
        int valueStart = 0;
        while (matcher.find()) {
            String value = trimTrailingSeparators(args.substring(valueStart, matcher.start()));
            arguments.put(previousPrefix, trimSpaces(value));
            previousPrefix = new Prefix(matcher.group(1).toLowerCase(Locale.ROOT));
            valueStart = matcher.end();
        }
        arguments.put(previousPrefix, trimSpaces(args.substring(valueStart)));
        return arguments;
    }

    /**
     * Removes ASCII whitespace immediately before a known parameter prefix.
     */
    private static String trimTrailingSeparators(String value) {
        int end = value.length();
        while (end > 0) {
            char last = value.charAt(end - 1);
            if (last != ' ' && (last < '\t' || last > '\r')) {
                break;
            }
            end--;
        }
        return value.substring(0, end);
    }

    /**
     * Removes ordinary spaces around values without silently discarding invalid control characters.
     */
    private static String trimSpaces(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == ' ') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == ' ') {
            end--;
        }
        return value.substring(start, end);
    }

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
