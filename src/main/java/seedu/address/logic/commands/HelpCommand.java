package seedu.address.logic.commands;

import java.util.Locale;

import seedu.address.model.Model;

/**
 * Formats help instructions for supported ClientDesk client and project commands.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = "Usage: help [COMMAND]";

    public static final String SHOWING_HELP_MESSAGE = ClientDeskHelp.getOverview();

    private final String command;

    /**
     * Creates a command that shows the supported ClientDesk command catalog.
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
        String message = command == null ? SHOWING_HELP_MESSAGE : ClientDeskHelp.getDetails(command);
        return new CommandResult(message, true, false);
    }
}
