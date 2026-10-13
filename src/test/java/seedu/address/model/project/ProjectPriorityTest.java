package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ProjectPriorityTest {

    @Test
    public void fromString_validPriority_returnsPriority() {
        assertEquals(ProjectPriority.LOW, ProjectPriority.fromString("low"));
        assertEquals(ProjectPriority.MEDIUM, ProjectPriority.fromString("medium"));
        assertEquals(ProjectPriority.HIGH, ProjectPriority.fromString("high"));
        assertEquals("low", ProjectPriority.LOW.toString());
        assertEquals("medium", ProjectPriority.MEDIUM.toString());
        assertEquals("high", ProjectPriority.HIGH.toString());
    }

    @Test
    public void fromString_invalidPriority_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> ProjectPriority.fromString(null));
        String[] invalidPriorities = {"", "urgent", "HIGH", "High", " high", "medium "};
        for (String priority : invalidPriorities) {
            assertThrows(IllegalArgumentException.class, ProjectPriority.MESSAGE_CONSTRAINTS, () ->
                    ProjectPriority.fromString(priority));
        }
    }
}
