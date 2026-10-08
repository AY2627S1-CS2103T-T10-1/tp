package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

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
        assertTrue(result.contains("agenda - "));
        assertEquals(expected, model);
    }

    @Test
    public void parse_commandName_showsFormatRulesAndExamples() throws Exception {
        String result = parser.parse("  ADD-CLIENT ").execute(new ModelManager()).getFeedbackToUser();
        assertTrue(result.contains("Format: add-client n/NAME p/PHONE e/EMAIL [t/TAG]..."));
        assertTrue(result.contains("7–15 digits"));
        assertTrue(result.contains("Examples: add-client"));
        assertTrue(parser.parse("agenda").execute(new ModelManager()).getFeedbackToUser()
                .contains("Format: agenda [--overdue]"));
    }

    @Test
    public void parse_eachMvpCommand_hasDetailedInstructions() throws Exception {
        for (String name : new String[] {"help", "add-client", "list", "view", "edit-client", "delete-client",
            "find", "add-project", "edit-project", "undo", "add-note", "follow-up", "agenda"}) {
            String result = parser.parse(name).execute(new ModelManager()).getFeedbackToUser();
            assertTrue(result.contains("Format: " + name));
            assertTrue(result.contains("Rules: "));
            assertTrue(result.contains("Examples: "));
        }
    }

    @Test
    public void parse_unknownCommand_reportsOriginalName() {
        assertThrows(ParseException.class, "Unknown command 'Missing'. Type help to see all commands.", () ->
            parser.parse("Missing"));
    }

    @Test
    public void parse_multipleArguments_reportsUsage() {
        assertThrows(ParseException.class, HelpCommand.MESSAGE_USAGE, () -> parser.parse("add-client agenda"));
    }
}
