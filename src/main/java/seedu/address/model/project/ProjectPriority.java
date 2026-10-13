package seedu.address.model.project;

import static java.util.Objects.requireNonNull;

/**
 * The three supported project priorities.
 */
public enum ProjectPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    public static final String MESSAGE_CONSTRAINTS = "Priority must be low, medium, or high.";

    private final String value;

    ProjectPriority(String value) {
        this.value = value;
    }

    /**
     * Parses a canonical priority name.
     *
     * @throws IllegalArgumentException if the value is not a supported priority.
     */
    public static ProjectPriority fromString(String value) {
        requireNonNull(value);
        for (ProjectPriority priority : values()) {
            if (priority.value.equals(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return value;
    }
}
