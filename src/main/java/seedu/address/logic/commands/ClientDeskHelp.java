package seedu.address.logic.commands;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides help for supported ClientDesk client and project commands.
 */
public class ClientDeskHelp {

    private static final String LEGACY_FIELD_RULES =
            "Name: 1–80 characters with a letter or number; letters, numbers, spaces, apostrophes, "
            + "hyphens, periods, & and parentheses are allowed.\n"
            + "Phone: at least 3 digits, without spaces, hyphens or +. Email: 3–254 characters, "
            + "one @, no spaces and a dotted domain whose labels do not start or end with a hyphen.\n"
            + "Address must not be blank. Tags: 1–30 letters, numbers or internal hyphens; case is preserved.\n"
            + "Use lowercase prefixes separated by spaces. Do not repeat n/, p/, e/ or a/. "
            + "Duplicates: same email ignoring case, or the same normalized name and phone.";

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
     * Returns the supported command names and short descriptions in display order.
     */
    public static String getOverview() {
        StringBuilder result = new StringBuilder("ClientDesk commands:\n");
        COMMANDS.forEach((name, help) -> result.append(name).append(" - ").append(help.purpose()).append("\n"));
        return result.append("Type help COMMAND to view syntax, rules and examples.").toString();
    }

    /**
     * Returns the detailed help for a supported command.
     */
    public static String getDetails(String command) {
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
                "help; help add-client; help remark"));
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
        commands.put("add", new CommandHelp(
                "Create a client with an address using the legacy command.",
                "add n/NAME p/PHONE e/EMAIL a/ADDRESS [t/TAG]...",
                "Name, phone, email and address are required; tags are optional and repeatable.\n"
                + LEGACY_FIELD_RULES + "\nUse add-client for formatted phone numbers and creation without an address.",
                "add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01"));
        commands.put("list", new CommandHelp(
                "Display all clients with indexes.",
                "list",
                "No parameters are needed; extra text is ignored. Clears the search filter. "
                + "Clients are sorted by name, ignoring case; ties use creation order.",
                "list"));
        commands.put("list-clients", new CommandHelp(
                "Display all clients with indexes.",
                "list-clients",
                "No parameters. Clients are sorted by name, ignoring case; ties use creation order."
                + " The existing `list` command remains available.",
                "list-clients"));
        commands.put("view", new CommandHelp(
                "Show a client's contact information, remark, tags and projects.",
                "view CLIENT_INDEX",
                "CLIENT_INDEX must be a displayed positive integer without a sign or decimal.",
                "view 1; view 12"));
        commands.put("edit-client", new CommandHelp(
                "Update a client's email while preserving the other fields.",
                "edit-client CLIENT_INDEX e/EMAIL",
                "Use an index from the currently displayed client list and supply one email. "
                + "Email must be valid, at most 254 characters and have a dot in its domain.\n"
                + "An email already used by another client, ignoring case, is rejected. "
                + "An unchanged email skips saving. The current client filter is preserved.",
                "edit-client 1 e/new@acme.sg; edit-client 2 e/new@northstar.io"));
        commands.put("edit", new CommandHelp(
                "Update a client's contact fields or replace tags using the legacy command.",
                "edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...",
                "INDEX is a positive integer from the currently displayed list. Supply at least one field. "
                + "Omitted fields, remarks and projects are preserved. A successful edit shows all clients.\n"
                + "Tags replace the entire tag set; an empty t/ clears all tags.\n" + LEGACY_FIELD_RULES,
                "edit 1 p/91234567 e/johndoe@example.com; edit 2 n/Betsy Crower t/"));
        commands.put("remark", new CommandHelp(
                "Set, replace or clear a client's remark.",
                "remark INDEX r/[REMARK]",
                "INDEX is a positive integer from the currently displayed client list. "
                + "The remark replaces any existing text; an empty r/ clears it. "
                + "A successful update shows all clients.",
                "remark 1 r/Prefers email contact.; remark 1 r/"));
        commands.put("delete-client", new CommandHelp(
                "Remove a client and all their data.",
                "delete-client CLIENT_INDEX",
                "CLIENT_INDEX must be a displayed positive integer. Deletion is saved immediately and cannot be "
                + "undone in the app.",
                "delete-client 3"));
        commands.put("delete", new CommandHelp(
                "Remove a client and their projects using the legacy command.",
                "delete INDEX",
                "INDEX is a positive integer from the currently displayed list, including find results. "
                + "Supply exactly one index. Deletion cannot be undone in the app.",
                "delete 1"));
        commands.put("find", new CommandHelp(
                "Locate clients by partial name, phone, email or tag.",
                "find KEYWORD [KEYWORD]...",
                "Each keyword is 1–50 characters. Matching ignores case. All keywords must match the"
                + " same client. If no clients match, the full client list is restored. "
                + "Check the displayed indexes before running an indexed command.",
                "find acme; find startup web-design; find 8123"));
        commands.put("add-project", new CommandHelp(
                "Record client work and its deadline.",
                "add-project CLIENT_INDEX title/TITLE deadline/DATE [status/STATUS] "
                + "[priority/PRIORITY]",
                "Use a displayed client index. TITLE: 1–100 printable characters with a letter or "
                + "number. Known whitespace-separated parameter prefixes delimit fields.\n"
                + "DATE: real yyyy-MM-dd date in years 0001–9999; past dates are allowed with an overdue warning.\n"
                + "STATUS: not-started (default) or in-progress. PRIORITY: low, medium (default) or high.\n"
                + "Duplicate: the same client, normalized title and deadline, regardless of status or priority.\n"
                + "Projects are saved before success; a failed save leaves data and the proposed ID unchanged.",
                "add-project 1 title/Website redesign deadline/2026-10-15"));
        commands.put("edit-project", new CommandHelp(
                "Update a project, including marking it completed.",
                "edit-project CLIENT_INDEX PROJECT_ID [title/TITLE] [deadline/DATE] [status/STATUS] "
                + "[priority/PRIORITY]",
                "Use a displayed client index and an existing project ID, both unsigned ASCII integers. "
                + "Supply at least "
                + "one field.\nTITLE: 1–100 printable characters with a letter or number. DATE: real yyyy-MM-dd "
                + "date, including past dates. STATUS: not-started, in-progress or completed. "
                + "PRIORITY: low, medium or high.\nOmitted fields and the project ID are preserved. "
                + "The same normalized title and deadline as another project for this client is rejected. "
                + "An unchanged update skips saving; failed saves leave all data unchanged.",
                "edit-project 1 2 status/completed priority/high"));
        commands.put("delete-project", new CommandHelp(
                "Remove a project from a client.",
                "delete-project CLIENT_INDEX PROJECT_ID",
                "Use a displayed client index and a project ID belonging to that client. IDs are client-scoped.",
                "delete-project 1 2"));
        commands.put("clear", new CommandHelp(
                "Remove every client and project.",
                "clear",
                "No parameters are needed; extra text is ignored. Clears all clients, including those hidden "
                + "by find. There is no confirmation and this cannot be undone in the app.",
                "clear"));
        commands.put("exit", new CommandHelp(
                "Close ClientDesk.",
                "exit",
                "No parameters are needed; extra text is ignored. Successful data changes are saved automatically.",
                "exit"));
        return Collections.unmodifiableMap(commands);
    }

    /**
     * Stores the purpose, syntax, rules and examples for one supported command.
     */
    private record CommandHelp(String purpose, String format, String rules, String examples) {
    }
}
