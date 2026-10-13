package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

/**
 * Parses arguments for adding a project to a displayed client.
 */
public class AddProjectCommandParser implements Parser<AddProjectCommand> {

    private static final Pattern PARAMETER_PREFIX = Pattern.compile(
            "(?<!\\S)(title/|deadline/|status/|priority/)", Pattern.CASE_INSENSITIVE);

    @Override
    public AddProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = tokenize(args);
        String preamble = arguments.getPreamble().strip();
        if (preamble.isEmpty() || preamble.split("\\s+").length != 1
                || arguments.getValue(PREFIX_TITLE).isEmpty() || arguments.getValue(PREFIX_DEADLINE).isEmpty()) {
            throw new ParseException(AddProjectCommand.MESSAGE_USAGE);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_TITLE, PREFIX_DEADLINE, PREFIX_STATUS, PREFIX_PRIORITY);

        Index index = parseClientIndex(preamble);
        try {
            ProjectTitle title = new ProjectTitle(arguments.getValue(PREFIX_TITLE).get());
            ProjectDeadline deadline = new ProjectDeadline(arguments.getValue(PREFIX_DEADLINE).get());
            ProjectStatus status = parseStatus(arguments.getValue(PREFIX_STATUS).orElse("not-started"));
            ProjectPriority priority = ProjectPriority.fromString(
                    arguments.getValue(PREFIX_PRIORITY).orElse("medium").toLowerCase(Locale.ROOT));
            return new AddProjectCommand(index, title, deadline, status, priority);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    /**
     * Extracts case-insensitive prefixes while preserving control characters inside field values.
     * Supported prefixes delimit fields; other slash-containing words remain part of the current value.
     */
    private ArgumentMultimap tokenize(String args) {
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
    private String trimTrailingSeparators(String value) {
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
    private String trimSpaces(String value) {
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

    /**
     * Parses an unsigned ASCII integer within the range supported by displayed client indexes.
     */
    private Index parseClientIndex(String value) throws ParseException {
        if (!value.matches("[0-9]+")) {
            throw new ParseException(AddProjectCommand.MESSAGE_INVALID_INDEX);
        }
        try {
            int oneBasedIndex = Integer.parseInt(value);
            if (oneBasedIndex < 1) {
                throw new ParseException(AddProjectCommand.MESSAGE_INVALID_INDEX);
            }
            return Index.fromOneBased(oneBasedIndex);
        } catch (NumberFormatException exception) {
            throw new ParseException(AddProjectCommand.MESSAGE_INVALID_INDEX, exception);
        }
    }

    /**
     * Parses a status that is permitted when creating a project.
     */
    private ProjectStatus parseStatus(String value) throws ParseException {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "not-started" -> ProjectStatus.NOT_STARTED;
            case "in-progress" -> ProjectStatus.IN_PROGRESS;
            default -> throw new ParseException(AddProjectCommand.MESSAGE_NEW_STATUS);
        };
    }
}
