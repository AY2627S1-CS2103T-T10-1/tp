package seedu.address.model.project;

/**
 * The persisted and user-visible stages of client work.
 */
public enum ProjectStatus {
    NOT_STARTED("not-started"),
    IN_PROGRESS("in-progress"),
    COMPLETED("completed");

    public static final String MESSAGE_CONSTRAINTS = "Status must be not-started, in-progress or completed.";

    private final String value;

    ProjectStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a status without imposing creation-only restrictions on editing or loading.
     */
    public static ProjectStatus fromValue(String value) {
        for (ProjectStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
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
