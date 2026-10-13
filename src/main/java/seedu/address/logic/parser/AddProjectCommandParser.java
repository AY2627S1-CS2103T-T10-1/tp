package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;

import java.util.Locale;
import java.util.Map;
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

    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");
    private static final Map<String, Prefix> SUPPORTED_PREFIXES = Map.of(
            "title/", PREFIX_TITLE, "deadline/", PREFIX_DEADLINE,
            "status/", PREFIX_STATUS, "priority/", PREFIX_PRIORITY);

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
        ProjectTitle title = parseTitle(arguments.getValue(PREFIX_TITLE).get());
        ProjectDeadline deadline = parseDeadline(arguments.getValue(PREFIX_DEADLINE).get());
        ProjectStatus status = parseStatus(arguments.getValue(PREFIX_STATUS).orElse("not-started"));
        ProjectPriority priority = parsePriority(arguments.getValue(PREFIX_PRIORITY).orElse("medium"));
        return new AddProjectCommand(index, title, deadline, status, priority);
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
            String prefixText = matcher.group(1);
            Prefix prefix = SUPPORTED_PREFIXES.get(prefixText.toLowerCase(Locale.ROOT));
            if (prefix == null) {
                continue;
            }
            String value = args.substring(valueStart, matcher.start()).replaceAll("\\s+$", "");
            arguments.put(previousPrefix, trimSpaces(value));
            previousPrefix = prefix;
            valueStart = matcher.end();
        }
        arguments.put(previousPrefix, trimSpaces(args.substring(valueStart)));
        return arguments;
    }

    /**
     * Removes ordinary spaces around values without silently discarding invalid control characters.
     */
    private String trimSpaces(String value) {
        return value.replaceAll("^ +| +$", "");
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
     * Parses a printable project title without replacing invalid characters.
     */
    private ProjectTitle parseTitle(String value) throws ParseException {
        try {
            return new ProjectTitle(value);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(ProjectTitle.MESSAGE_CONSTRAINTS, exception);
        }
    }

    /**
     * Parses a real calendar date in the required ISO format, including past dates.
     */
    private ProjectDeadline parseDeadline(String value) throws ParseException {
        try {
            return new ProjectDeadline(value);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(ProjectDeadline.MESSAGE_CONSTRAINTS, exception);
        }
    }

    /**
     * Parses a status that is permitted when creating a project.
     */
    private ProjectStatus parseStatus(String value) throws ParseException {
        try {
            ProjectStatus status = ProjectStatus.fromString(value.toLowerCase(Locale.ROOT));
            if (status == ProjectStatus.COMPLETED) {
                throw new ParseException(AddProjectCommand.MESSAGE_NEW_STATUS);
            }
            return status;
        } catch (IllegalArgumentException exception) {
            throw new ParseException(AddProjectCommand.MESSAGE_NEW_STATUS, exception);
        }
    }

    /**
     * Parses one of the three supported priority levels.
     */
    private ProjectPriority parsePriority(String value) throws ParseException {
        try {
            return ProjectPriority.fromString(value.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(ProjectPriority.MESSAGE_CONSTRAINTS, exception);
        }
    }
}
