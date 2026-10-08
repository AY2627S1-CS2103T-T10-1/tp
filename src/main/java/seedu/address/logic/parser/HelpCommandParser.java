package seedu.address.logic.parser;

import java.util.Locale;

import seedu.address.logic.commands.ClientDeskHelp;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the optional command name for ClientDesk help.
 */
public class HelpCommandParser implements Parser<HelpCommand> {

    @Override
    public HelpCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty()) {
            return new HelpCommand();
        }
        String[] words = trimmed.split("\\s+");
        if (words.length != 1) {
            throw new ParseException(HelpCommand.MESSAGE_USAGE);
        }
        String command = words[0].toLowerCase(Locale.ROOT);
        if (!ClientDeskHelp.containsCommand(command)) {
            throw new ParseException("Unknown command '" + trimmed + "'. Type help to see all commands.");
        }
        return new HelpCommand(command);
    }
}
