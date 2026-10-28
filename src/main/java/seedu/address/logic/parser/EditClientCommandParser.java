package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditClientCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;

/**
 * Parses the email-only first increment of edit-client.
 */
public class EditClientCommandParser implements Parser<EditClientCommand> {

    public static final String MESSAGE_INVALID_EMAIL = "Enter an email such as name@example.com.";

    @Override
    public EditClientCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            throw new ParseException(EditClientCommand.MESSAGE_USAGE);
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_EMAIL);
        String preamble = arguments.getPreamble();
        if (preamble.split("\\s+").length != 1) {
            throw new ParseException(EditClientCommand.MESSAGE_USAGE);
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(preamble);
        } catch (ParseException pe) {
            throw new ParseException(EditClientCommand.MESSAGE_INVALID_INDEX, pe);
        }
        if (arguments.getValue(PREFIX_EMAIL).isEmpty()) {
            throw new ParseException(EditClientCommand.MESSAGE_NO_FIELD);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_EMAIL);
        Email email = ParserUtil.parseEmail(arguments.getValue(PREFIX_EMAIL).get());
        return new EditClientCommand(index, email);
    }
}
