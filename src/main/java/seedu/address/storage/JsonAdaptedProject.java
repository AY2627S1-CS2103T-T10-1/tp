package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;

/**
 * JSON representation of a project, with dates and enums in the command vocabulary.
 */
class JsonAdaptedProject {
    private final Integer id;
    private final String title;
    private final String deadline;
    private final String status;
    private final String priority;

    /**
     * Creates storage data without accepting missing or invalid values into the model.
     */
    @JsonCreator
    public JsonAdaptedProject(@JsonProperty("id") Integer id, @JsonProperty("title") String title,
            @JsonProperty("deadline") String deadline, @JsonProperty("status") String status,
            @JsonProperty("priority") String priority) {
        this.id = id;
        this.title = title;
        this.deadline = deadline;
        this.status = status;
        this.priority = priority;
    }

    /**
     * Copies immutable model values to JSON fields.
     */
    public JsonAdaptedProject(Project source) {
        this(source.getId(), source.getTitle(), source.getDeadline().toString(),
                source.getStatus().toString(), source.getPriority().toString());
    }

    /**
     * Validates stored data through the same domain rules used by command parsing.
     */
    public Project toModelType() throws IllegalValueException {
        if (id == null || title == null || deadline == null || status == null || priority == null) {
            throw new IllegalValueException("Each project requires id, title, deadline, status and priority.");
        }
        try {
            return new Project(id, title, Project.parseDeadline(deadline), ProjectStatus.fromValue(status),
                    ProjectPriority.fromValue(priority));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
