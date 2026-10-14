package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.testutil.PersonBuilder;

public class DeleteProjectCommandTest {
    private static final Project FIRST_PROJECT = project(1, "Website", "2026-10-15");
    private static final Project SECOND_PROJECT = project(2, "Brand refresh", "2026-10-20");

    @Test
    public void execute_validProjectId_removesOnlyProjectAndKeepsNextId() throws Exception {
        Person client = client(List.of(FIRST_PROJECT, SECOND_PROJECT), 3);
        Model model = modelWith(client);
        Model expectedModel = modelWith(client.withProjects(List.of(SECOND_PROJECT), 3));

        assertCommandSuccess(new DeleteProjectCommand(Index.fromOneBased(1), 1), model,
                "Deleted project #1 from Amy Bee.", expectedModel);
        assertEquals(List.of(SECOND_PROJECT), model.getAddressBook().getPersonList().getFirst().getProjects());
        assertEquals(3, model.getAddressBook().getPersonList().getFirst().getNextProjectId());
        assertEquals(List.of(FIRST_PROJECT, SECOND_PROJECT), client.getProjects());
    }

    @Test
    public void execute_filteredIndex_deletesProjectFromDisplayedClient() throws Exception {
        Person hiddenClient = new PersonBuilder().withName("Alpha").withEmail("alpha@example.com")
                .withProjects(List.of(FIRST_PROJECT), 2).build();
        Person visibleClient = new PersonBuilder().withName("Zulu").withEmail("zulu@example.com")
                .withProjects(List.of(FIRST_PROJECT), 2).build();
        Model model = new ModelManager();
        model.addPerson(hiddenClient);
        model.addPerson(visibleClient);
        model.updateFilteredPersonList(person -> person.equals(visibleClient));

        new DeleteProjectCommand(Index.fromOneBased(1), 1).execute(model);

        assertEquals(List.of(), model.getAddressBook().getPersonList().get(1).getProjects());
        assertEquals(List.of(FIRST_PROJECT), model.getAddressBook().getPersonList().get(0).getProjects());
    }

    @Test
    public void execute_unknownProjectId_failsWithoutChangingClient() {
        Person client = client(List.of(FIRST_PROJECT), 2);
        Model model = modelWith(client);

        CommandException exception = assertThrows(CommandException.class, () ->
                new DeleteProjectCommand(Index.fromOneBased(1), 2).execute(model));

        assertEquals("No project with ID 2 exists for this client.", exception.getMessage());
        assertEquals(client, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void execute_invalidDisplayedClient_failsWithoutChangingData() {
        Model model = modelWith(client(List.of(FIRST_PROJECT), 2));
        model.updateFilteredPersonList(person -> false);

        assertCommandFailure(new DeleteProjectCommand(Index.fromOneBased(1), 1), model,
                DeleteProjectCommand.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void constructor_nonPositiveProjectId_rejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeleteProjectCommand(Index.fromOneBased(1), 0));
    }

    @Test
    public void equalsAndHashCode_compareClientAndProjectIds() {
        DeleteProjectCommand command = new DeleteProjectCommand(Index.fromOneBased(1), 3);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new DeleteProjectCommand(Index.fromOneBased(1), 3)));
        assertEquals(command.hashCode(), new DeleteProjectCommand(Index.fromOneBased(1), 3).hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("delete-project"));
        assertFalse(command.equals(new DeleteProjectCommand(Index.fromOneBased(2), 3)));
        assertFalse(command.equals(new DeleteProjectCommand(Index.fromOneBased(1), 4)));
    }

    private static Project project(int id, String title, String deadline) {
        return new Project(id, new ProjectTitle(title), new ProjectDeadline(deadline), ProjectStatus.NOT_STARTED,
                ProjectPriority.MEDIUM);
    }

    private static Person client(List<Project> projects, long nextProjectId) {
        return new PersonBuilder().withProjects(projects, nextProjectId).build();
    }

    private static Model modelWith(Person client) {
        Model model = new ModelManager();
        model.addPerson(client);
        return model;
    }
}
