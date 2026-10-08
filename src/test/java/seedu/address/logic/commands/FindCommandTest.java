package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests for {@code FindCommand} and the model's displayed indexes.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        FindCommand command = prepareCommand("first");
        assertTrue(command.equals(command));
        assertTrue(command.equals(prepareCommand("first")));
        assertFalse(command.equals(prepareCommand("second")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
    }

    @Test
    public void execute_allKeywordsMatch_findsClients() {
        CommandResult result = prepareCommand("MEI friends").execute(model);
        assertEquals("Found 2 matching clients.", result.getFeedbackToUser());
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatches_restoresFullList() {
        prepareCommand("Alice").execute(model);
        CommandResult result = prepareCommand("Kurz Kunz").execute(model);
        assertEquals("No clients matched: Kurz Kunz.", result.getFeedbackToUser());
        assertEquals(getTypicalPersons(), model.getFilteredPersonList());
    }

    @Test
    public void execute_repeatedSearch_searchesEntireAddressBook() {
        prepareCommand("Benson").execute(model);
        prepareCommand("PAUL").execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAddressBook_showsNoMatches() {
        model = new ModelManager();
        assertEquals("No clients matched: acme.", prepareCommand("acme").execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_equalNames_preservesCreationOrder() {
        Person first = new PersonBuilder().withName("acme").withPhone("11111111").build();
        Person second = new PersonBuilder().withName("ACME").withPhone("22222222").build();
        model = new ModelManager(new AddressBookBuilder().withPerson(first).withPerson(second).build(),
                new UserPrefs());
        prepareCommand("2222").execute(model);
        prepareCommand("acme").execute(model);
        assertEquals(List.of(first, second), model.getFilteredPersonList());
        Person editedFirst = new PersonBuilder(first).withPhone("33333333").build();
        model.setPerson(first, editedFirst);
        assertEquals(List.of(editedFirst, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_indexedCommands_useDisplayedIndexes() throws Exception {
        AddressBook addressBook = new AddressBookBuilder().withPerson(DANIEL).withPerson(BENSON)
                .withPerson(ALICE).build();
        model = new ModelManager(addressBook, new UserPrefs());
        prepareCommand("meier").execute(model);
        new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone("11223344").build()).execute(model);
        Person editedClient = new PersonBuilder(BENSON).withPhone("11223344").build();
        assertEquals(List.of(DANIEL, editedClient, ALICE), model.getAddressBook().getPersonList());
        prepareCommand("meier").execute(model);
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        assertFalse(model.hasPerson(editedClient));
        assertEquals(List.of(DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void toStringMethod() {
        PersonContainsKeywordsPredicate predicate = new PersonContainsKeywordsPredicate(List.of("keyword"));
        assertEquals(FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}",
                new FindCommand(predicate).toString());
    }

    @Test
    public void constructor_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindCommand(null));
    }

    /**
     * Creates a search command from space-separated {@code keywords}.
     */
    private FindCommand prepareCommand(String keywords) {
        return new FindCommand(new PersonContainsKeywordsPredicate(List.of(keywords.split("\\s+"))));
    }
}
