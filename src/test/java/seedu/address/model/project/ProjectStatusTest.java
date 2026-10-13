package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ProjectStatusTest {

    @Test
    public void fromString_validStatus_returnsStatus() {
        assertEquals(ProjectStatus.NOT_STARTED, ProjectStatus.fromString("not-started"));
        assertEquals(ProjectStatus.IN_PROGRESS, ProjectStatus.fromString("in-progress"));
        assertEquals(ProjectStatus.COMPLETED, ProjectStatus.fromString("completed"));
        assertEquals("not-started", ProjectStatus.NOT_STARTED.toString());
        assertEquals("in-progress", ProjectStatus.IN_PROGRESS.toString());
        assertEquals("completed", ProjectStatus.COMPLETED.toString());
    }

    @Test
    public void fromString_invalidStatus_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> ProjectStatus.fromString(null));
        String[] invalidStatuses = {"", "done", "NOT_STARTED", "Not-started", "in progress", " completed"};
        for (String status : invalidStatuses) {
            assertThrows(IllegalArgumentException.class, ProjectStatus.MESSAGE_CONSTRAINTS, () ->
                    ProjectStatus.fromString(status));
        }
    }
}
