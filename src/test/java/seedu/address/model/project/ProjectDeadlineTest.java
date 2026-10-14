package seedu.address.model.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class ProjectDeadlineTest {

    @Test
    public void constructor_invalidDeadline_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new ProjectDeadline(null));
        String[] invalidDates = {"", " ", "2026-02-29", "1900-02-29", "2026-04-31", "2026-00-10", "2026-13-01",
            "2026-10-00", "2026-10-32", "0000-01-01", "10000-01-01", "-0001-01-01", "+2026-10-15",
            "2026-1-15", "2026-10-5", "26-10-15", "２０２６-１０-１５", "2026-10-15 ", " 2026-10-15",
            "2026-10-15\n", "2026-10-15T00:00:00"};
        for (String date : invalidDates) {
            assertFalse(ProjectDeadline.isValidDeadline(date));
            assertThrows(IllegalArgumentException.class, ProjectDeadline.MESSAGE_CONSTRAINTS, () ->
                    new ProjectDeadline(date));
        }
    }

    @Test
    public void constructor_validDeadline_preservesDate() {
        String[] validDates = {"0001-01-01", "9999-12-31", "2000-02-29", "2024-02-29", "2026-10-15"};
        for (String date : validDates) {
            assertTrue(ProjectDeadline.isValidDeadline(date));
            ProjectDeadline deadline = new ProjectDeadline(date);
            assertEquals(date, deadline.toString());
            assertEquals(LocalDate.parse(date), deadline.getDate());
        }
    }

    @Test
    public void equalsAndHashCode_compareDates() {
        ProjectDeadline deadline = new ProjectDeadline("2026-10-15");
        assertEquals(deadline, deadline);
        assertEquals(deadline, new ProjectDeadline("2026-10-15"));
        assertEquals(deadline.hashCode(), new ProjectDeadline("2026-10-15").hashCode());
        assertFalse(deadline.equals(null));
        assertFalse(deadline.equals("2026-10-15"));
        assertFalse(deadline.equals(new ProjectDeadline("2026-10-16")));
    }
}
