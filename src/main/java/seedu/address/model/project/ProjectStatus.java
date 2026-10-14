package seedu.address.model.project;

import static java.util.Objects.requireNonNull;

/**
 * Project progress. Completed projects can be loaded, but cannot be created by add-project.
 */
public enum ProjectStatus {
    NOT_STARTED("not-started"),
    IN_PROGRESS("in-progress"),
    COMPLETED("completed");

    public static final String MESSAGE_CONSTRAINTS = "Status must be not-started, in-progress, or completed.";

    private final String value;

    ProjectStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a canonical status name.
     *
     * @throws IllegalArgumentException if the value is not a supported status.
     */
    public static ProjectStatus fromString(String value) {
        requireNonNull(value);
        for (ProjectStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return value;
    }
}
