package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.commands.EditProjectCommand.EditProjectDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;

/**
 * Parses an existing client's stable project ID and one or more updates.
 */
public class EditProjectCommandParser implements Parser<EditProjectCommand> {
    private static final Pattern PARAMETER_PREFIX = Pattern.compile(
            "(?<!\\S)(title/|deadline/|status/|priority/)", Pattern.CASE_INSENSITIVE);

    @Override
    public EditProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = tokenize(args);
        String[] identifiers = arguments.getPreamble().strip().split("\\s+");
        if (identifiers.length != 2) {
            throw new ParseException(EditProjectCommand.MESSAGE_USAGE);
        }
        Index clientIndex = parseIdentifier(identifiers[0], ParserUtil.MESSAGE_INVALID_INDEX);
        int projectId = parseIdentifier(identifiers[1], Project.MESSAGE_ID_CONSTRAINTS).getOneBased();
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_TITLE, PREFIX_DEADLINE, PREFIX_STATUS, PREFIX_PRIORITY);
        return new EditProjectCommand(clientIndex, projectId, parseDescriptor(arguments));
    }

    private EditProjectDescriptor parseDescriptor(ArgumentMultimap arguments) throws ParseException {
        try {
            String title = arguments.getValue(PREFIX_TITLE).orElse(null);
            LocalDate deadline = arguments.getValue(PREFIX_DEADLINE)
                    .map(value -> new ProjectDeadline(value).getDate()).orElse(null);
            ProjectStatus status = arguments.getValue(PREFIX_STATUS)
                    .map(value -> ProjectStatus.fromString(value.toLowerCase(Locale.ROOT))).orElse(null);
            ProjectPriority priority = arguments.getValue(PREFIX_PRIORITY)
                    .map(value -> ProjectPriority.fromString(value.toLowerCase(Locale.ROOT))).orElse(null);
            return new EditProjectDescriptor(title, deadline, status, priority);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

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
     * Trims ordinary spaces without hiding invalid control characters in a title.
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
     * Uses the same unsigned ASCII identifier form as Add Project.
     */
    private Index parseIdentifier(String value, String message) throws ParseException {
        if (!value.matches("[0-9]+")) {
            throw new ParseException(message);
        }
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException exception) {
            throw new ParseException(message, exception);
        }
    }
}
