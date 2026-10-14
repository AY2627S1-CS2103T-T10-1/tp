package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.testutil.PersonBuilder;

public class AddProjectCommandTest {

    private static final Clock TODAY = Clock.fixed(Instant.parse("2026-10-13T12:00:00Z"), ZoneOffset.UTC);

    @Test
    public void execute_pastTodayAndFuture_warnsOnlyForPastDate() throws Exception {
        for (String date : List.of("2026-10-12", "2026-10-13", "2026-10-14")) {
            ModelManager model = new ModelManager();
            Person client = new PersonBuilder().withName("Acme Studio").build();
            model.addPerson(client);
            CommandResult result = command(1, "Website", date).execute(model);
            String expected = "Added project #1 to Acme Studio.";
            if (date.equals("2026-10-12")) {
                expected += "\n" + AddProjectCommand.MESSAGE_OVERDUE;
            }
            assertEquals(expected, result.getFeedbackToUser());
            Person updated = model.getFilteredPersonList().getFirst();
            assertEquals(2, updated.getNextProjectId());
            assertEquals(ProjectStatus.NOT_STARTED, updated.getProjects().getFirst().getStatus());
            assertEquals(ProjectPriority.MEDIUM, updated.getProjects().getFirst().getPriority());
            assertTrue(result.getClientToSelect().isEmpty());
            assertTrue(client.getProjects().isEmpty());
        }
    }

    @Test
    public void execute_duplicateTitleAndDeadline_ignoresCaseSpacesStatusPriorityAndLocale() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        command(1, "I Design", "2026-10-15").execute(model);
        Person before = model.getFilteredPersonList().getFirst();
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            AddProjectCommand duplicate = new AddProjectCommand(Index.fromOneBased(1),
                    new ProjectTitle("i\u00a0\u2003DESIGN"), new ProjectDeadline("2026-10-15"),
                    ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH, TODAY);
            CommandException error = assertThrows(CommandException.class, () -> duplicate.execute(model));
            assertEquals("This project already exists for " + before.getName() + ".", error.getMessage());
            assertEquals(before, model.getFilteredPersonList().getFirst());
        } finally {
            Locale.setDefault(previousLocale);
        }
    }

    @Test
    public void execute_distinctDatesAndClients_allocateIndependentIds() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Zulu").withEmail("zulu@example.com").build());
        model.addPerson(new PersonBuilder().withName("Alpha").withEmail("alpha@example.com").build());
        command(1, "Website", "2026-10-15").execute(model);
        command(1, "Website", "2026-10-16").execute(model);
        command(2, "Website", "2026-10-15").execute(model);
        assertEquals(List.of(1, 2), model.getFilteredPersonList().get(0).getProjects().stream()
                .map(project -> project.getId()).toList());
        assertEquals(1, model.getFilteredPersonList().get(1).getProjects().getFirst().getId());
    }

    @Test
    public void execute_exhaustedIds_failsWithoutChangingClient() {
        ModelManager model = new ModelManager();
        Person exhausted = new PersonBuilder().build().withProjects(List.of(), Person.MAX_NEXT_PROJECT_ID);
        model.addPerson(exhausted);
        CommandException error = assertThrows(CommandException.class, () ->
                command(1, "Website", "2026-10-15").execute(model));
        assertEquals(AddProjectCommand.MESSAGE_ID_EXHAUSTED, error.getMessage());
        assertEquals(exhausted, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_lastAvailableId_succeedsWithoutOverflow() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build().withProjects(List.of(), Integer.MAX_VALUE));
        command(1, "Website", "2026-10-15").execute(model);
        Person updated = model.getFilteredPersonList().getFirst();
        assertEquals(Integer.MAX_VALUE, updated.getProjects().getFirst().getId());
        assertEquals(Person.MAX_NEXT_PROJECT_ID, updated.getNextProjectId());
    }

    @Test
    public void execute_missingDisplayedClient_throwsActionableError() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        model.updateFilteredPersonList(person -> false);
        CommandException error = assertThrows(CommandException.class, () ->
                command(1, "Website", "2026-10-15").execute(model));
        assertEquals("No client at index 1.", error.getMessage());
        assertTrue(model.getAddressBook().getPersonList().getFirst().getProjects().isEmpty());
    }

    @Test
    public void constructor_completedOrNullInput_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new AddProjectCommand(Index.fromOneBased(1),
                new ProjectTitle("Website"), new ProjectDeadline("2026-10-15"), ProjectStatus.COMPLETED,
                ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () -> new AddProjectCommand(null, new ProjectTitle("Website"),
                new ProjectDeadline("2026-10-15"), ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () -> command(1, "Website", "2026-10-15").execute(null));
    }

    @Test
    public void equalsAndHashCode_compareUserSuppliedFields() {
        AddProjectCommand value = command(1, "Website", "2026-10-15");
        assertEquals(value, value);
        assertEquals(value, command(1, "Website", "2026-10-15"));
        assertEquals(value.hashCode(), command(1, "Website", "2026-10-15").hashCode());
        assertNotEquals(value, null);
        assertNotEquals(value, "Website");
        assertNotEquals(value, command(2, "Website", "2026-10-15"));
        assertNotEquals(value, command(1, "Other", "2026-10-15"));
        assertNotEquals(value, command(1, "Website", "2026-10-16"));
        assertNotEquals(value, new AddProjectCommand(Index.fromOneBased(1), new ProjectTitle("Website"),
                new ProjectDeadline("2026-10-15"), ProjectStatus.IN_PROGRESS, ProjectPriority.MEDIUM));
        assertNotEquals(value, new AddProjectCommand(Index.fromOneBased(1), new ProjectTitle("Website"),
                new ProjectDeadline("2026-10-15"), ProjectStatus.NOT_STARTED, ProjectPriority.HIGH));
        assertTrue(value.toString().contains("Website"));
    }

    private static AddProjectCommand command(int index, String title, String deadline) {
        return new AddProjectCommand(Index.fromOneBased(index), new ProjectTitle(title), new ProjectDeadline(deadline),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM, TODAY);
    }
}
