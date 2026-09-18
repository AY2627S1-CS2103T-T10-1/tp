package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
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
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RemarkCommand(null, new Remark("note")));
        assertThrows(NullPointerException.class, () -> new RemarkCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RemarkCommand(INDEX_FIRST_PERSON,
                new Remark("note")).execute(null));
    }

    @Test
    public void execute_addRemark_preservesOtherFields() {
        assertRemarkSuccess(INDEX_FIRST_PERSON, "Likes baseball");
    }

    @Test
    public void execute_replaceRemark_success() {
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withRemark("Old note").build());
        assertRemarkSuccess(INDEX_FIRST_PERSON, "New note");
    }

    @Test
    public void execute_removeRemark_success() {
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withRemark("Old note").build());
        assertRemarkSuccess(INDEX_FIRST_PERSON, "");
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndShowsAll() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess(INDEX_FIRST_PERSON, "Filtered person note");
        assertEquals(getTypicalAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
        assertEquals("Filtered person note", model.getFilteredPersonList().get(1).getRemark().value);
        assertEquals("", model.getFilteredPersonList().get(0).getRemark().value);
    }

    @Test
    public void execute_invalidUnfilteredIndex_failure() {
        Index index = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(index, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidFilteredIndex_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyAddressBook_failure() {
        model = new ModelManager();
        assertCommandFailure(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equalsAndHashCode() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        RemarkCommand copy = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(copy));
        assertEquals(command.hashCode(), copy.hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("note"));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("different"))));
    }

    @Test
    public void toStringMethod() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        assertEquals(RemarkCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON + ", remark=note}",
                command.toString());
    }

    /**
     * Checks the feedback, replacement person, unchanged fields and the expected full model.
     */
    private void assertRemarkSuccess(Index index, String value) {
        Person original = model.getFilteredPersonList().get(index.getZeroBased());
        Person edited = new PersonBuilder(original).withRemark(value).build();
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, edited);
        String message = value.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
        assertCommandSuccess(new RemarkCommand(index, new Remark(value)), model,
                String.format(message, Messages.format(edited)), expected);
    }
}
