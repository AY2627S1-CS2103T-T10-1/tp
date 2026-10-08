package seedu.address.logic.commands;

import java.util.Locale;

import seedu.address.model.Model;

/**
 * Formats full help instructions for every command for display.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = "Usage: help [COMMAND]";

    public static final String SHOWING_HELP_MESSAGE = ClientDeskHelp.overview();

    private final String command;

    /**
     * Creates a command that lists all MVP commands.
     */
    public HelpCommand() {
        command = null;
    }

    /**
     * Creates a command that shows detailed help for a supported command.
     */
    public HelpCommand(String command) {
        this.command = command.toLowerCase(Locale.ROOT);
    }

    @Override
    public CommandResult execute(Model model) {
        String message = command == null ? SHOWING_HELP_MESSAGE : ClientDeskHelp.details(command);
        return new CommandResult(message, true, false);
    }
}
