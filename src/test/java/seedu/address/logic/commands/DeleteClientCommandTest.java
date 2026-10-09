package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.TypicalIndexes;

public class DeleteClientCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_success() {
        Person clientToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(clientToDelete);

        assertCommandSuccess(new DeleteClientCommand(INDEX_FIRST_PERSON), model,
                String.format(DeleteClientCommand.MESSAGE_SUCCESS, clientToDelete.getName().fullName), expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new DeleteClientCommand(invalidIndex), model,
                DeleteClientCommand.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void equals() {
        DeleteClientCommand first = new DeleteClientCommand(INDEX_FIRST_PERSON);
        assertTrue(first.equals(first));
        assertTrue(first.equals(new DeleteClientCommand(INDEX_FIRST_PERSON)));
        assertFalse(first.equals(new DeleteClientCommand(TypicalIndexes.INDEX_SECOND_PERSON)));
        assertFalse(first.equals(null));
        assertFalse(first.equals("not a command"));
    }

    @Test
    public void toStringMethod() {
        DeleteClientCommand command = new DeleteClientCommand(INDEX_FIRST_PERSON);
        assertEquals(DeleteClientCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}",
                command.toString());
    }
}
