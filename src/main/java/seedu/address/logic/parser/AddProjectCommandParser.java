package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;

import java.util.Locale;

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

    @Override
    public AddProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ProjectParserUtil.tokenize(args);
        String preamble = arguments.getPreamble().strip();
        if (preamble.isEmpty() || preamble.split("\\s+").length != 1
                || arguments.getValue(PREFIX_TITLE).isEmpty() || arguments.getValue(PREFIX_DEADLINE).isEmpty()) {
            throw new ParseException(AddProjectCommand.MESSAGE_USAGE);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_TITLE, PREFIX_DEADLINE, PREFIX_STATUS, PREFIX_PRIORITY);

        Index index = ProjectParserUtil.parseIdentifier(preamble, AddProjectCommand.MESSAGE_INVALID_INDEX);
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
