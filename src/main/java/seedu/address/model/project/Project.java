package seedu.address.model.project;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * An immutable project with an ID unique to its owning client.
 */
public final class Project {

    public static final String MESSAGE_ID_CONSTRAINTS = "Project ID must be a positive integer.";

    private final int id;
    private final ProjectTitle title;
    private final ProjectDeadline deadline;
    private final ProjectStatus status;
    private final ProjectPriority priority;

    /**
     * Constructs a project with a positive ID and validated fields.
     */
    public Project(int id, ProjectTitle title, ProjectDeadline deadline, ProjectStatus status,
            ProjectPriority priority) {
        requireAllNonNull(title, deadline, status, priority);
        checkArgument(id > 0, MESSAGE_ID_CONSTRAINTS);
        this.id = id;
        this.title = title;
        this.deadline = deadline;
        this.status = status;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public ProjectTitle getTitle() {
        return title;
    }

    public ProjectDeadline getDeadline() {
        return deadline;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public ProjectPriority getPriority() {
        return priority;
    }

    /**
     * Returns whether projects match by title and deadline, independent of their IDs or progress.
     */
    public boolean isSameProject(Project other) {
        return other != null && title.isSameTitle(other.title)
                && deadline.equals(other.deadline);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Project otherProject)) {
            return false;
        }
        return id == otherProject.id && title.equals(otherProject.title) && deadline.equals(otherProject.deadline)
                && status == otherProject.status && priority == otherProject.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, deadline, status, priority);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("title", title)
                .add("deadline", deadline)
                .add("status", status)
                .add("priority", priority)
                .toString();
    }
}
