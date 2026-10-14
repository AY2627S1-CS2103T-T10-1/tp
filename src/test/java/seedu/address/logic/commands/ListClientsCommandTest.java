package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
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

public class ListClientsCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsAllClients() {
        assertCommandSuccess(new ListClientsCommand(), model, ListClientsCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsAllClients() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListClientsCommand(), model, ListClientsCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_preservesClientProjects() {
        Project project = new Project(1, new ProjectTitle("Website"), new ProjectDeadline("2026-10-15"),
                ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);
        Person client = new PersonBuilder().withProjects(List.of(project), 2).build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(client);
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        model.updateFilteredPersonList(person -> false);

        assertCommandSuccess(new ListClientsCommand(), model, ListClientsCommand.MESSAGE_SUCCESS, expectedModel);

        assertEquals(List.of(project), model.getFilteredPersonList().getFirst().getProjects());
        assertEquals(2, model.getFilteredPersonList().getFirst().getNextProjectId());
    }
}
