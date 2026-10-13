package seedu.address.model.project;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * A real calendar date in the four-digit-year ISO format used by project commands and storage.
 */
public final class ProjectDeadline {

    public static final String MESSAGE_CONSTRAINTS = "Deadline must be a real date in yyyy-MM-dd format.";

    public final LocalDate date;

    /**
     * Constructs a deadline from an exact yyyy-MM-dd date with year 0001–9999.
     */
    public ProjectDeadline(String deadline) {
        requireNonNull(deadline);
        checkArgument(isValidDeadline(deadline), MESSAGE_CONSTRAINTS);
        date = LocalDate.parse(deadline);
    }

    /**
     * Constructs a deadline from a date with year 0001–9999.
     */
    public ProjectDeadline(LocalDate deadline) {
        requireNonNull(deadline);
        checkArgument(deadline.getYear() >= 1 && deadline.getYear() <= 9999, MESSAGE_CONSTRAINTS);
        date = deadline;
    }

    /**
     * Returns whether the value has the exact required format and represents a real date.
     */
    public static boolean isValidDeadline(String test) {
        requireNonNull(test);
        if (!test.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            return false;
        }
        try {
            return LocalDate.parse(test).getYear() >= 1;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return date.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ProjectDeadline otherDeadline && date.equals(otherDeadline.date));
    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }
}
