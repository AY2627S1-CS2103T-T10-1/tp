package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class HelpCommandParserTest {

    private final HelpCommandParser parser = new HelpCommandParser();

    @Test
    public void parse_noArguments_listsCommandsWithoutChangingData() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expected = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        String result = parser.parse("  ").execute(model).getFeedbackToUser();
        assertTrue(result.contains("add-client - "));
        assertTrue(result.contains("remark - "));
        for (String name : new String[] {"undo", "add-note", "follow-up", "agenda"}) {
            assertFalse(result.contains(name + " - "));
        }
        assertEquals(expected, model);
    }

    @Test
    public void parse_commandName_showsFormatRulesAndExamples() throws Exception {
        String result = parser.parse("  ADD-CLIENT ").execute(new ModelManager()).getFeedbackToUser();
        assertTrue(result.contains("Format: add-client n/NAME p/PHONE e/EMAIL [t/TAG]..."));
        assertTrue(result.contains("7–15 digits"));
        assertTrue(result.contains("Examples: add-client"));
        assertTrue(parser.parse("  ReMaRk ").execute(new ModelManager()).getFeedbackToUser()
                .contains("Format: remark INDEX r/[REMARK]"));
    }

    @Test
    public void parse_eachSupportedCommand_hasDetailedInstructions() throws Exception {
        Model model = new ModelManager();
        String overview = parser.parse("").execute(model).getFeedbackToUser();
        for (String name : new String[] {"help", "add-client", "add", "list", "list-clients", "view",
            "edit-client", "edit", "remark", "delete-client", "delete", "find", "add-project", "edit-project",
            "delete-project", "clear", "exit"}) {
            String result = parser.parse(name).execute(model).getFeedbackToUser();
            assertTrue(overview.contains(name + " - "), name);
            assertTrue(result.contains("Format: " + name), name);
            assertTrue(result.contains("Rules: "), name);
            assertTrue(result.contains("Examples: "), name);
            String mixedCaseName = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
            assertEquals(result, parser.parse("  " + mixedCaseName + "  ").execute(model).getFeedbackToUser());
            assertEquals(result, new AddressBookParser().parseCommand("HeLp " + name.toUpperCase(Locale.ROOT))
                    .execute(model).getFeedbackToUser());
        }
    }

    @Test
    public void parse_futureCommand_reportsUnknownNameWhileCurrentCommandWorks() throws Exception {
        assertTrue(parser.parse("edit-project").execute(new ModelManager()).getFeedbackToUser()
                .contains("Format: edit-project CLIENT_INDEX PROJECT_ID"));
        for (String name : new String[] {"undo", "add-note", "follow-up", "agenda"}) {
            String expectedMessage = "Unknown command '" + name + "'. Type help to see all commands.";
            assertThrows(ParseException.class, expectedMessage, () -> parser.parse(name));
        }
    }

    @Test
    public void parse_unknownCommand_reportsOriginalName() {
        assertThrows(ParseException.class, "Unknown command 'Missing'. Type help to see all commands.", () ->
            parser.parse("Missing"));
    }

    @Test
    public void parse_multipleArguments_reportsUsage() {
        assertThrows(ParseException.class, HelpCommand.MESSAGE_USAGE, () -> parser.parse("add-client remark"));
    }
}
