package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteProjectCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the displayed client index and client-scoped project ID. */
public class DeleteProjectCommandParser implements Parser<DeleteProjectCommand> {

    @Override
    public DeleteProjectCommand parse(String args) throws ParseException {
        String[] identifiers = args.trim().split("\\s+");
        if (identifiers.length != 2) {
            throw new ParseException(DeleteProjectCommand.MESSAGE_USAGE);
        }
        Index clientIndex;
        int projectId;
        try {
            clientIndex = ParserUtil.parseIndex(identifiers[0]);
            projectId = parseProjectId(identifiers[1]);
        } catch (ParseException exception) {
            throw new ParseException(DeleteProjectCommand.MESSAGE_USAGE, exception);
        }
        return new DeleteProjectCommand(clientIndex, projectId);
    }

    private int parseProjectId(String value) throws ParseException {
        if (!value.matches("[1-9][0-9]*")) {
            throw new ParseException("Project ID must be a positive integer.");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ParseException("Project ID must be a positive integer.", exception);
        }
    }
}
