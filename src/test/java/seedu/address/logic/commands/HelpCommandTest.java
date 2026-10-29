package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE, true, false);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void execute_editClient_describesEmailOnlyUpdate() {
        CommandResult result = new HelpCommand("EDIT-CLIENT").execute(model);
        String instructions = result.getFeedbackToUser();
        assertTrue(instructions.contains("Format: edit-client CLIENT_INDEX e/EMAIL"));
        assertTrue(instructions.contains("Examples: edit-client 1 e/new@acme.sg; edit-client 2 e/new@northstar.io"));
        assertFalse(instructions.contains("remove-tag/"));
        assertFalse(instructions.contains("p/PHONE"));
        assertTrue(result.isShowHelp());
        assertFalse(result.isExit());
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_find_explainsFullListResetAndPreservesModel() {
        String instructions = new HelpCommand("find").execute(model).getFeedbackToUser();
        assertTrue(instructions.contains("If no clients match, the full client list is restored."));
        assertTrue(instructions.contains("Check the displayed indexes"));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_view_describesDisplayedClientFields() {
        String instructions = new HelpCommand("view").execute(model).getFeedbackToUser();
        assertTrue(instructions.contains("contact information, remark, tags and projects"));
        assertFalse(instructions.contains("notes"));
        assertFalse(instructions.contains("follow-up"));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_remark_describesSettingClearingAndFilteredIndex() {
        String instructions = new HelpCommand("remark").execute(model).getFeedbackToUser();
        assertTrue(instructions.contains("Format: remark INDEX r/[REMARK]"));
        assertTrue(instructions.contains("currently displayed client list"));
        assertTrue(instructions.contains("empty r/ clears"));
        assertTrue(instructions.contains("shows all clients"));
        assertEquals(expectedModel, model);
    }
}
