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
        Index clientIndex = ProjectParserUtil.parseIdentifier(identifiers[0], ParserUtil.MESSAGE_INVALID_INDEX);
        int projectId = ProjectParserUtil.parseIdentifier(identifiers[1],
                "Project ID must be a positive integer.").getOneBased();
        return new DeleteProjectCommand(clientIndex, projectId);
    }
}
