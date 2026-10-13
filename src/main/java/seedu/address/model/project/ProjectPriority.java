package seedu.address.model.project;

/**
 * The persisted and user-visible priority of client work.
 */
public enum ProjectPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    public static final String MESSAGE_CONSTRAINTS = "Priority must be low, medium or high.";

    private final String value;

    ProjectPriority(String value) {
        this.value = value;
    }

    /**
     * Parses a priority using the command vocabulary, ignoring case.
     */
    public static ProjectPriority fromValue(String value) {
        for (ProjectPriority priority : values()) {
            if (priority.value.equalsIgnoreCase(value)) {
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
