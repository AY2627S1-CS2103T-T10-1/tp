package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditProjectCommand.EditProjectDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

/**
 * Tests stable project identity, duplicate handling and displayed client indexes.
 */
public class EditProjectCommandTest {
    private static final LocalDate DEADLINE = LocalDate.of(2026, 10, 15);
    private static final Project WEBSITE = project(11, "Website");
    private static final Project LOGO = project(3, "Logo");

    @Test
    public void execute_partialEdit_preservesIdOtherProjectsAndClientDetails() throws Exception {
        Person original = ALICE.withProjects(List.of(WEBSITE, LOGO), 12);
        Model model = new ModelManager();
        model.addPerson(original);
        new EditProjectCommand(Index.fromOneBased(1), 11,
                new EditProjectDescriptor(null, null, ProjectStatus.COMPLETED, null)).execute(model);
        Project updated = new Project(11, new ProjectTitle("Website"), new ProjectDeadline(DEADLINE),
                ProjectStatus.COMPLETED, ProjectPriority.MEDIUM);
        assertEquals(original.withProjects(List.of(updated, LOGO), 12), model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_filteredClientIndex_changesOnlySelectedClientsProject() throws Exception {
        Model model = new ModelManager();
        Person alice = ALICE.withProjects(List.of(WEBSITE), 12);
        Person benson = BENSON.withProjects(List.of(WEBSITE), 12);
        model.addPerson(alice);
        model.addPerson(benson);
        model.updateFilteredPersonList(person -> person.getName().equals(BENSON.getName()));
        new EditProjectCommand(Index.fromOneBased(1), 11,
                new EditProjectDescriptor("New work", null, null, null)).execute(model);
        assertEquals(alice, model.getAddressBook().getPersonList().getFirst());
        assertEquals("New work", model.getFilteredPersonList().getFirst().getProjects().getFirst().getTitle().value);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_duplicateTitleAndDeadline_rejectsWithoutChanges() {
        Model model = new ModelManager();
        Person original = ALICE.withProjects(List.of(WEBSITE, LOGO), 12);
        model.addPerson(original);
        CommandException exception = assertThrows(CommandException.class, () -> new EditProjectCommand(
                Index.fromOneBased(1), 11, new EditProjectDescriptor(" logo ", null,
                        ProjectStatus.COMPLETED, ProjectPriority.HIGH)).execute(model));
        assertEquals(EditProjectCommand.MESSAGE_DUPLICATE, exception.getMessage());
        assertEquals(original, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_sameTitleDifferentDeadline_succeeds() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE.withProjects(List.of(WEBSITE, LOGO), 12));
        new EditProjectCommand(Index.fromOneBased(1), 11,
                new EditProjectDescriptor("Logo", DEADLINE.plusDays(1), null, null)).execute(model);
        assertEquals(DEADLINE.plusDays(1),
                model.getFilteredPersonList().getFirst().getProjects().getFirst().getDeadline().date);
    }

    @Test
    public void execute_invalidClientOrProjectId_rejectsWithoutChanges() {
        Model model = new ModelManager();
        Person original = ALICE.withProjects(List.of(WEBSITE), 12);
        model.addPerson(original);
        EditProjectDescriptor descriptor = new EditProjectDescriptor(null, null, ProjectStatus.COMPLETED, null);
        EditProjectCommand invalidClient = new EditProjectCommand(Index.fromOneBased(2), 11, descriptor);
        EditProjectCommand invalidProject = new EditProjectCommand(Index.fromOneBased(1), 1, descriptor);
        assertEquals("No client at index 2.",
                assertThrows(CommandException.class, () -> invalidClient.execute(model)).getMessage());
        assertEquals("No project with ID 1 for Alice Pauline.",
                assertThrows(CommandException.class, () -> invalidProject.execute(model)).getMessage());
        assertEquals(original, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void equalsAndHashCode_compareClientIdAndFields() {
        EditProjectDescriptor descriptor = new EditProjectDescriptor("Logo", null, null, null);
        EditProjectCommand command = new EditProjectCommand(Index.fromOneBased(1), 11, descriptor);
        EditProjectCommand equivalent = new EditProjectCommand(Index.fromOneBased(1), 11, descriptor);
        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, "edit-project");
        assertNotEquals(command, new EditProjectCommand(Index.fromOneBased(2), 11, descriptor));
        assertNotEquals(command, new EditProjectCommand(Index.fromOneBased(1), 3, descriptor));
        assertNotEquals(command, new EditProjectCommand(Index.fromOneBased(1), 11,
                new EditProjectDescriptor("Website", null, null, null)));
    }

    private static Project project(int id, String title) {
        return new Project(id, new ProjectTitle(title), new ProjectDeadline(DEADLINE),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
    }
}
