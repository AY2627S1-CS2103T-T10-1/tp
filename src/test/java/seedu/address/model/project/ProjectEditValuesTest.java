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
public class ProjectEditValuesTest {
    private static final LocalDate DEADLINE = LocalDate.of(2026, 10, 15);

    @Test
    public void isValidTitle_printableTextAndLengthBoundaries() {
        for (String title : List.of("A", "x".repeat(100), " Logo — phase 2! ", "设计方案", "😀A")) {
            assertTrue(ProjectTitle.isValidTitle(title), title);
        }
        for (String title : List.of("", " ", "---", "😀", "x".repeat(101), "Logo\nrevision", "Logo\u0000")) {
            assertFalse(ProjectTitle.isValidTitle(title), title);
        }
        assertTrue(ProjectTitle.isValidTitle("𠀀".repeat(100)));
        assertFalse(ProjectTitle.isValidTitle("𠀀".repeat(101)));
    }

    @Test
    public void parseDeadline_realDatesIncludingPastAndLeapDays() {
        for (String date : List.of("2024-02-29", "2000-01-01", "0001-01-01", "9999-12-31")) {
            assertEquals(LocalDate.parse(date), new ProjectDeadline(date).getDate());
        }
        for (String date : List.of("2026-02-29", "2026-04-31", "2026-13-01", "2026-00-01", "2026-10-00",
                "2026-1-01", "26-10-15", "0000-01-01", "2026-10-15 trailing", "")) {
            assertEquals(ProjectDeadline.MESSAGE_CONSTRAINTS,
                    assertThrows(IllegalArgumentException.class, () -> new ProjectDeadline(date)).getMessage());
        }
    }

    @Test
    public void constructor_validatesDomainBoundariesAndTrimsTitle() {
        assertEquals("Logo", createProject(1, " Logo ").getTitle().value);
        assertThrows(IllegalArgumentException.class, () -> createProject(0, "Logo"));
        assertThrows(IllegalArgumentException.class, () -> createProject(1, ""));
        assertThrows(IllegalArgumentException.class, () -> new Project(1, new ProjectTitle("Logo"),
                new ProjectDeadline(LocalDate.of(0, 1, 1).toString()),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM));
        assertThrows(NullPointerException.class, () -> new Project(1, new ProjectTitle("Logo"),
                new ProjectDeadline(DEADLINE.toString()), null, ProjectPriority.LOW));
    }

    @Test
    public void isSameProject_ignoresIdsStatusPriorityCaseAndRepeatedSpaces() {
        Project original = createProject(3, "Logo  Refresh");
        Project equivalentWork = new Project(11, new ProjectTitle("logo refresh"),
                new ProjectDeadline(DEADLINE.toString()),
                ProjectStatus.COMPLETED, ProjectPriority.HIGH);
        assertTrue(original.isSameProject(equivalentWork));
        assertNotEquals(original, equivalentWork);
        assertFalse(original.isSameProject(createProject(3, "Different work")));
        assertFalse(original.isSameProject(new Project(3, original.getTitle(),
                new ProjectDeadline(DEADLINE.plusDays(1).toString()),
                original.getStatus(), original.getPriority())));
    }

    @Test
    public void equalsAndHashCode_includeAllPersistedFields() {
        Project original = createProject(3, "Logo");
        assertEquals(original, original);
        assertEquals(original, createProject(3, "Logo"));
        assertEquals(original.hashCode(), createProject(3, "Logo").hashCode());
        assertNotEquals(original, null);
        assertNotEquals(original, "Logo");
        assertNotEquals(original, createProject(4, "Logo"));
        assertNotEquals(original, createProject(3, "Website"));
        assertNotEquals(original, new Project(3, new ProjectTitle("Logo"), new ProjectDeadline(DEADLINE.toString()),
                ProjectStatus.COMPLETED, ProjectPriority.MEDIUM));
        assertNotEquals(original, new Project(3, new ProjectTitle("Logo"), new ProjectDeadline(DEADLINE.toString()),
                ProjectStatus.NOT_STARTED, ProjectPriority.HIGH));
    }

    @Test
    public void enumValues_acceptCommandVocabularyAndRejectUnknownValues() {
        assertEquals(ProjectStatus.COMPLETED, ProjectStatus.fromString("completed"));
        assertEquals(ProjectStatus.IN_PROGRESS, ProjectStatus.fromString("in-progress"));
        assertEquals(ProjectPriority.HIGH, ProjectPriority.fromString("high"));
        assertThrows(IllegalArgumentException.class, () -> ProjectStatus.fromString("pending"));
        assertThrows(IllegalArgumentException.class, () -> ProjectPriority.fromString("urgent"));
    }

    private Project createProject(int id, String title) {
        return new Project(id, new ProjectTitle(title), new ProjectDeadline(DEADLINE.toString()),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
    }
}
