package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests view through the main parser, including filtered list indexes and read-only behavior.
 */
public class ViewCommandTest {

    @Test
    public void execute_filteredList_displaysClientWithoutChangingModel() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Benson Meier"));
        Model expected = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expected.updateFilteredPersonList(person -> person.getName().fullName.equals("Benson Meier"));
        String feedback = new AddressBookParser().parseCommand("view 1").execute(model).getFeedbackToUser();
        assertEquals("Name: Benson Meier\nPhone: 98765432\nEmail: johnd@example.com"
                + "\nAddress: 311, Clementi Ave 2, #02-25\nRemark: \nTags: friends, owesMoney", feedback);
        assertEquals(expected, model);
    }

    @Test
    public void execute_outOfRange_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        CommandException exception = assertThrows(
                CommandException.class, () -> new ViewCommand(Index.fromOneBased(999)).execute(model));
        assertEquals(ViewCommand.MESSAGE_INVALID_INDEX, exception.getMessage());
    }

    @Test
    public void execute_projectsLoadedOutOfOrder_displaysAscendingIdsWithoutMutation() throws Exception {
        Project second = new Project(2, new ProjectTitle("Second"), new ProjectDeadline("2026-10-15"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
        Project first = new Project(1, new ProjectTitle("First"), new ProjectDeadline("2026-10-15"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
        Person client = new PersonBuilder().build().withProjects(List.of(second, first), 3);
        Model model = new ModelManager();
        model.addPerson(client);
        String feedback = new ViewCommand(Index.fromOneBased(1)).execute(model).getFeedbackToUser();
        assertTrue(feedback.indexOf("#1 | First") < feedback.indexOf("#2 | Second"));
        assertEquals(List.of(second, first), client.getProjects());
        assertEquals(client, new PersonBuilder(client).build());
    }

    @Test
    public void parse_invalidIndexes_throwsParseException() {
        for (String input : new String[] {"view", "view 0", "view -1", "view abc", "view 1 extra"}) {
            assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand(input));
        }
    }
    @Test
    public void equalsAndHashCode_compareDisplayedIndex() {
        ViewCommand command = new ViewCommand(Index.fromOneBased(1));
        ViewCommand equivalent = new ViewCommand(Index.fromOneBased(1));
        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, "view");
        assertNotEquals(command, new ViewCommand(Index.fromOneBased(2)));
    }

}
