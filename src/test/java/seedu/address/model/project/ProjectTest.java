package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ProjectTest {

    private static final ProjectTitle TITLE = new ProjectTitle("Website redesign");
    private static final ProjectDeadline DEADLINE = new ProjectDeadline("2026-10-15");
    private static final Project PROJECT =
            new Project(1, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);

    @Test
    public void constructor_invalidFields_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Project(0, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        assertThrows(IllegalArgumentException.class, () ->
                new Project(-1, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () ->
                new Project(1, null, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () ->
                new Project(1, TITLE, null, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () ->
                new Project(1, TITLE, DEADLINE, null, ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () ->
                new Project(1, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, null));
    }

    @Test
    public void constructor_validFields_preservesFields() {
        assertEquals(1, PROJECT.getId());
        assertEquals(TITLE, PROJECT.getTitle());
        assertEquals(DEADLINE, PROJECT.getDeadline());
        assertEquals(ProjectStatus.NOT_STARTED, PROJECT.getStatus());
        assertEquals(ProjectPriority.MEDIUM, PROJECT.getPriority());
        assertEquals(Integer.MAX_VALUE,
                new Project(Integer.MAX_VALUE, TITLE, DEADLINE, ProjectStatus.COMPLETED, ProjectPriority.LOW).getId());
    }

    @Test
    public void isSameProject_ignoresIdCaseSpacesStatusAndPriority() {
        Project duplicate = new Project(2, new ProjectTitle(" WEBSITE\u00a0  REDESIGN "), DEADLINE,
                ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);
        assertTrue(PROJECT.isSameProject(PROJECT));
        assertTrue(PROJECT.isSameProject(duplicate));
        assertFalse(PROJECT.isSameProject(null));
        assertFalse(PROJECT.isSameProject(new Project(1, new ProjectTitle("Landing page"), DEADLINE,
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM)));
        assertFalse(PROJECT.isSameProject(new Project(1, TITLE, new ProjectDeadline("2026-10-16"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM)));
    }

    @Test
    public void equalsAndHashCode_compareAllFields() {
        Project copy = new Project(1, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
        assertEquals(PROJECT, PROJECT);
        assertEquals(PROJECT, copy);
        assertEquals(PROJECT.hashCode(), copy.hashCode());
        assertFalse(PROJECT.equals(null));
        assertFalse(PROJECT.equals(1));
        assertFalse(PROJECT.equals(new Project(2, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM)));
        assertFalse(PROJECT.equals(new Project(1, new ProjectTitle("website redesign"), DEADLINE,
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM)));
        assertFalse(PROJECT.equals(new Project(1, TITLE, new ProjectDeadline("2026-10-16"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM)));
        assertFalse(PROJECT.equals(new Project(1, TITLE, DEADLINE, ProjectStatus.IN_PROGRESS, ProjectPriority.MEDIUM)));
        assertFalse(PROJECT.equals(new Project(1, TITLE, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.HIGH)));
    }

    @Test
    public void toStringMethod_includesAllFields() {
        assertEquals(Project.class.getCanonicalName() + "{id=1, title=Website redesign, deadline=2026-10-15, "
                + "status=not-started, priority=medium}", PROJECT.toString());
    }
}
