package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests shared project validation and the distinction between identity and duplicate work.
 */
public class ProjectTest {
    private static final LocalDate DEADLINE = LocalDate.of(2026, 10, 15);

    @Test
    public void isValidTitle_printableTextAndLengthBoundaries() {
        for (String title : List.of("A", "x".repeat(100), " Logo — phase 2! ", "设计方案", "😀A")) {
            assertTrue(Project.isValidTitle(title), title);
        }
        for (String title : List.of("", " ", "---", "😀", "x".repeat(101), "Logo\nrevision", "Logo\u0000")) {
            assertFalse(Project.isValidTitle(title), title);
        }
        assertTrue(Project.isValidTitle("𠀀".repeat(100)));
        assertFalse(Project.isValidTitle("𠀀".repeat(101)));
    }

    @Test
    public void parseDeadline_realDatesIncludingPastAndLeapDays() {
        for (String date : List.of("2024-02-29", "2000-01-01", "0001-01-01", "9999-12-31")) {
            assertEquals(LocalDate.parse(date), Project.parseDeadline(date));
        }
        for (String date : List.of("2026-02-29", "2026-04-31", "2026-13-01", "2026-00-01", "2026-10-00",
                "2026-1-01", "26-10-15", "0000-01-01", "2026-10-15 trailing", "")) {
            assertEquals(Project.MESSAGE_DEADLINE_CONSTRAINTS,
                    assertThrows(IllegalArgumentException.class, () -> Project.parseDeadline(date)).getMessage());
        }
    }

    @Test
    public void constructor_validatesDomainBoundariesAndTrimsTitle() {
        assertEquals("Logo", project(1, " Logo ").getTitle());
        assertThrows(IllegalArgumentException.class, () -> project(0, "Logo"));
        assertThrows(IllegalArgumentException.class, () -> project(1, ""));
        assertThrows(IllegalArgumentException.class, () -> new Project(1, "Logo", LocalDate.of(0, 1, 1),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () -> new Project(1, "Logo", DEADLINE, null, ProjectPriority.LOW));
    }

    @Test
    public void isSameWork_ignoresIdsStatusPriorityCaseAndRepeatedSpaces() {
        Project original = project(3, "Logo  Refresh");
        Project equivalentWork = new Project(11, "logo refresh", DEADLINE,
                ProjectStatus.COMPLETED, ProjectPriority.HIGH);
        assertTrue(original.isSameWork(equivalentWork));
        assertNotEquals(original, equivalentWork);
        assertFalse(original.isSameWork(project(3, "Different work")));
        assertFalse(original.isSameWork(new Project(3, original.getTitle(), DEADLINE.plusDays(1),
                original.getStatus(), original.getPriority())));
    }

    @Test
    public void equalsAndHashCode_includeAllPersistedFields() {
        Project original = project(3, "Logo");
        assertEquals(original, original);
        assertEquals(original, project(3, "Logo"));
        assertEquals(original.hashCode(), project(3, "Logo").hashCode());
        assertNotEquals(original, null);
        assertNotEquals(original, "Logo");
        assertNotEquals(original, project(4, "Logo"));
        assertNotEquals(original, project(3, "Website"));
        assertNotEquals(original, new Project(3, "Logo", DEADLINE, ProjectStatus.COMPLETED, ProjectPriority.MEDIUM));
        assertNotEquals(original, new Project(3, "Logo", DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.HIGH));
    }

    @Test
    public void enumValues_acceptCommandVocabularyAndRejectUnknownValues() {
        assertEquals(ProjectStatus.COMPLETED, ProjectStatus.fromValue("COMPLETED"));
        assertEquals(ProjectStatus.IN_PROGRESS, ProjectStatus.fromValue("in-progress"));
        assertEquals(ProjectPriority.HIGH, ProjectPriority.fromValue("HIGH"));
        assertThrows(IllegalArgumentException.class, () -> ProjectStatus.fromValue("pending"));
        assertThrows(IllegalArgumentException.class, () -> ProjectPriority.fromValue("urgent"));
    }

    private Project project(int id, String title) {
        return new Project(id, title, DEADLINE, ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
    }
}
