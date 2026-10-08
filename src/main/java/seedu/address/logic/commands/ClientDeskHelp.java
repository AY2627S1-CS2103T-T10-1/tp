package seedu.address.logic.commands;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides command descriptions and syntax from the ClientDesk MVP specification.
 */
public class ClientDeskHelp {

    private static final Map<String, CommandHelp> COMMANDS = createCommands();

    private ClientDeskHelp() {
    }

    /**
     * Returns whether the given command has a help entry.
     */
    public static boolean containsCommand(String command) {
        return COMMANDS.containsKey(command);
    }

    /**
     * Returns all MVP command names and short descriptions.
     */
    public static String overview() {
        StringBuilder result = new StringBuilder("ClientDesk commands:\n");
        COMMANDS.forEach((name, help) -> result.append(name).append(" - ").append(help.purpose()).append("\n"));
        return result.append("Type help COMMAND to view syntax, rules and examples.").toString();
    }

    /**
     * Returns the detailed help for a supported command.
     */
    public static String details(String command) {
        CommandHelp help = COMMANDS.get(command);
        return help.purpose() + "\nFormat: " + help.format() + "\nRules: " + help.rules()
                + "\nExamples: " + help.examples();
    }

    /**
     * Creates the help entries in their display order.
     */
    private static Map<String, CommandHelp> createCommands() {
        Map<String, CommandHelp> commands = new LinkedHashMap<>();
        commands.put("help", new CommandHelp(
                "Discover commands and view exact syntax.",
                "help [COMMAND]",
                "COMMAND is optional and case-insensitive. Supply at most one valid command name.",
                "help; help add-client; help agenda"));
        commands.put("add-client", new CommandHelp(
                "Create a client with contact information and optional tags.",
                "add-client n/NAME p/PHONE e/EMAIL [t/TAG]...",
                "Name: 1–80 characters with at least one letter or number; letters, numbers, spaces, "
                + "apostrophes, hyphens, periods, & and parentheses are allowed.\n"
                + "Phone: 7–15 digits; optional leading +, spaces and hyphens.\n"
                + "Email: 3–254 characters, one @, non-empty local and domain parts, no spaces, "
                + "and a dot in the domain. Domain labels cannot start or end with a hyphen.\n"
                + "Tags: 1–30 lowercase letters, numbers or internal hyphens; no leading/trailing hyphen. "
                + "Repeat t/ to add more tags; repeated tags are stored once.\n"
                + "Name, phone and email are required. Duplicates: same normalized email, or the same "
                + "normalized name and phone. Same name alone is allowed.",
                "add-client n/Acme Studio p/+65 8123 4567 e/hello@acme.sg; add-client n/Northstar "
                + "Labs p/91234567 e/alex@northstar.io t/startup t/web-design"));
        commands.put("list", new CommandHelp(
                "Display all clients with indexes.",
                "list",
                "No parameters. Clients are sorted by name, ignoring case; ties use creation order.",
                "list"));
        commands.put("view", new CommandHelp(
                "Show a client record, projects, notes, tags and follow-up.",
                "view CLIENT_INDEX",
                "CLIENT_INDEX must be a displayed positive integer without a sign or decimal.",
                "view 1; view 12"));
        commands.put("edit-client", new CommandHelp(
                "Update client contact information or tags.",
                "edit-client CLIENT_INDEX [n/NAME] [p/PHONE] [e/EMAIL] [t/TAG]... [remove-"
                + "tag/TAG]...",
                "Use a displayed client index and at least one field. Field rules match add-client.\n"
                + "Removal wins when the same tag is both added and removed.",
                "edit-client 1 p/+65 8777 2233; edit-client 2 e/new@northstar.io t/retainer remove-"
                + "tag/startup"));
        commands.put("delete-client", new CommandHelp(
                "Remove a client and all their data.",
                "delete-client CLIENT_INDEX",
                "CLIENT_INDEX must be a displayed positive integer. Use undo to restore a deletion.",
                "delete-client 3"));
        commands.put("find", new CommandHelp(
                "Locate clients by partial name, phone, email or tag.",
                "find KEYWORD [KEYWORD]...",
                "Each keyword is 1–50 characters. Matching ignores case. All keywords must match the"
                + " same client.",
                "find acme; find startup web-design; find 8123"));
        commands.put("add-project", new CommandHelp(
                "Record client work and its deadline.",
                "add-project CLIENT_INDEX title/TITLE deadline/DATE [status/STATUS] "
                + "[priority/PRIORITY]",
                "Use a displayed client index. TITLE: 1–100 printable characters with a letter or "
                + "number.\nDATE: real yyyy-MM-dd date; past dates are allowed.\nSTATUS: not-started "
                + "(default) or in-progress. PRIORITY: low, medium (default) or high.",
                "add-project 1 title/Website redesign deadline/2026-10-15"));
        commands.put("edit-project", new CommandHelp(
                "Update a project, including marking it completed.",
                "edit-project CLIENT_INDEX PROJECT_ID [title/TITLE] [deadline/DATE] [status/STATUS] "
                + "[priority/PRIORITY]",
                "Use a displayed client index and an existing positive project ID. Supply at least "
                + "one field.\nField rules match add-project; STATUS also accepts completed.",
                "edit-project 1 2 status/completed priority/high"));
        commands.put("undo", new CommandHelp(
                "Reverse the most recent data-changing command.",
                "undo",
                "No parameters. Restored records keep their original IDs.",
                "undo"));
        commands.put("add-note", new CommandHelp(
                "Record a dated note for a client.",
                "add-note CLIENT_INDEX text/TEXT",
                "Use a displayed client index. TEXT: 1–1000 printable characters; whitespace is "
                + "normalized.",
                "add-note 1 text/Discussed homepage direction."));
        commands.put("follow-up", new CommandHelp(
                "Set or replace a client follow-up date and detail.",
                "follow-up CLIENT_INDEX on/DATE [details/TEXT]",
                "Use a displayed client index. DATE: real yyyy-MM-dd date.\nOptional TEXT: 1–200 "
                + "printable characters.",
                "follow-up 1 on/2026-09-21 details/Ask for design approval"));
        commands.put("agenda", new CommandHelp(
                "View pending projects and follow-ups together.",
                "agenda [--overdue]",
                "No argument shows today through the next 30 days. --overdue shows items before "
                + "today.",
                "agenda; agenda --overdue"));
        return Collections.unmodifiableMap(commands);
    }

    private record CommandHelp(String purpose, String format, String rules, String examples) {
    }
}
