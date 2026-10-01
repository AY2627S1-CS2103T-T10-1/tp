package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addRemark_success() {
        Remark remark = new Remark("Likes baseball");
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, remark);
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(originalPerson).withRemark(remark.value).build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(originalPerson, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeRemark_success() {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithRemark = new PersonBuilder(originalPerson).withRemark("Temporary remark").build();
        model.setPerson(originalPerson, personWithRemark);

        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        Person editedPerson = new PersonBuilder(personWithRemark).withRemark("").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithRemark, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS,
                Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        RemarkCommand command = new RemarkCommand(invalidIndex, new Remark("Likes baseball"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Likes baseball"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Different"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
    }

    @Test
    public void toStringMethod() {
        Remark remark = new Remark("Likes baseball");
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, remark);
        String expected = RemarkCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", remark=" + remark + "}";

        assertEquals(expected, command.toString());
    }
}
