package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;

import java.time.LocalDate;
import java.util.Locale;

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

    @Override
    public EditProjectCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ProjectParserUtil.tokenize(args);
        String[] identifiers = arguments.getPreamble().strip().split("\\s+");
        if (identifiers.length != 2) {
            throw new ParseException(EditProjectCommand.MESSAGE_USAGE);
        }
        Index clientIndex = ProjectParserUtil.parseIdentifier(identifiers[0], ParserUtil.MESSAGE_INVALID_INDEX);
        int projectId = ProjectParserUtil.parseIdentifier(identifiers[1], Project.MESSAGE_ID_CONSTRAINTS)
                .getOneBased();
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

}
