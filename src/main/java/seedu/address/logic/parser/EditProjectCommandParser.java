package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.commands.EditProjectCommand.EditProjectDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.project.Project;
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
    private static final Set<String> PREFIXES = Set.of("title/", "deadline/", "status/", "priority/");
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");

    @Override
    public EditProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalizePrefixes(args),
                PREFIX_TITLE, PREFIX_DEADLINE, PREFIX_STATUS, PREFIX_PRIORITY);
        String[] identifiers = arguments.getPreamble().split("\\s+");
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
            LocalDate deadline = arguments.getValue(PREFIX_DEADLINE).map(Project::parseDeadline).orElse(null);
            ProjectStatus status = arguments.getValue(PREFIX_STATUS).map(ProjectStatus::fromValue).orElse(null);
            ProjectPriority priority = arguments.getValue(PREFIX_PRIORITY).map(ProjectPriority::fromValue).orElse(null);
            return new EditProjectDescriptor(title, deadline, status, priority);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    /**
     * Rejects unknown parameters and normalizes only prefix names, leaving text values intact.
     */
    private String normalizePrefixes(String args) throws ParseException {
        Matcher matcher = PARAMETER_PREFIX.matcher(" " + args.replace('\t', ' '));
        StringBuilder normalized = new StringBuilder();
        while (matcher.find()) {
            String prefix = matcher.group(1).toLowerCase(Locale.ROOT);
            if (!PREFIXES.contains(prefix)) {
                throw new ParseException("Unknown project parameter: " + matcher.group(1)
                        + ".\n" + EditProjectCommand.MESSAGE_USAGE);
            }
            matcher.appendReplacement(normalized, Matcher.quoteReplacement(prefix));
        }
        matcher.appendTail(normalized);
        return normalized.toString();
    }
}
