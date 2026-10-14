package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
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
    private static final Prefix PREFIX_TITLE = new Prefix("title/");
    private static final Prefix PREFIX_DEADLINE = new Prefix("deadline/");
    private static final Prefix PREFIX_STATUS = new Prefix("status/");
    private static final Prefix PREFIX_PRIORITY = new Prefix("priority/");
    private static final Map<String, Prefix> PREFIXES = Map.of(
            "title/", PREFIX_TITLE, "deadline/", PREFIX_DEADLINE,
            "status/", PREFIX_STATUS, "priority/", PREFIX_PRIORITY);
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");

    @Override
    public EditProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = tokenize(args);
        String[] identifiers = arguments.getPreamble().strip().split("\\s+");
        if (identifiers.length != 2) {
            throw new ParseException(EditProjectCommand.MESSAGE_USAGE);
        }
        Index clientIndex = ParserUtil.parseIndex(identifiers[0]);
        int projectId;
        try {
            projectId = ParserUtil.parseIndex(identifiers[1]).getOneBased();
        } catch (ParseException exception) {
            throw new ParseException(Project.MESSAGE_ID_CONSTRAINTS, exception);
        }
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

    /**
     * Uses the same delimiters as Add Project: recognized prefixes separate fields, while
     * ordinary slash-containing words remain title text. Control characters are not normalized away.
     */
    private ArgumentMultimap tokenize(String args) {
        ArgumentMultimap arguments = new ArgumentMultimap();
        Matcher matcher = PARAMETER_PREFIX.matcher(args);
        Prefix previousPrefix = new Prefix("");
        int valueStart = 0;
        while (matcher.find()) {
            Prefix prefix = PREFIXES.get(matcher.group(1).toLowerCase(Locale.ROOT));
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

    private String trimSpaces(String value) {
        return value.replaceAll("^ +| +$", "");
    }
}
