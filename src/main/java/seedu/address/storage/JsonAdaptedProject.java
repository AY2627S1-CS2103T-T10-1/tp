package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

/**
 * Jackson-friendly project data. JSON nodes retain the original types so invalid scalars cannot be coerced.
 */
class JsonAdaptedProject {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Project's %s field is missing!";

    private final JsonNode id;
    private final JsonNode title;
    private final JsonNode deadline;
    private final JsonNode status;
    private final JsonNode priority;

    /**
     * Constructs an adapted project from its stored fields.
     */
    @JsonCreator
    public JsonAdaptedProject(@JsonProperty("id") JsonNode id, @JsonProperty("title") JsonNode title,
            @JsonProperty("deadline") JsonNode deadline, @JsonProperty("status") JsonNode status,
            @JsonProperty("priority") JsonNode priority) {
        this.id = id;
        this.title = title;
        this.deadline = deadline;
        this.status = status;
        this.priority = priority;
    }

    /**
     * Copies a validated project for serialization.
     */
    public JsonAdaptedProject(Project source) {
        id = IntNode.valueOf(source.getId());
        title = TextNode.valueOf(source.getTitle().value);
        deadline = TextNode.valueOf(source.getDeadline().toString());
        status = TextNode.valueOf(source.getStatus().toString());
        priority = TextNode.valueOf(source.getPriority().toString());
    }

    /**
     * Converts the stored fields into a project, rejecting missing fields and invalid values.
     */
    public Project toModelType() throws IllegalValueException {
        requireField(id, "id");
        if (!id.isIntegralNumber() || !id.canConvertToInt() || id.intValue() <= 0) {
            throw new IllegalValueException(Project.MESSAGE_ID_CONSTRAINTS);
        }
        String modelTitle = readText(title, "title");
        String modelDeadline = readText(deadline, "deadline");
        String modelStatus = readText(status, "status");
        String modelPriority = readText(priority, "priority");
        try {
            return new Project(id.intValue(), new ProjectTitle(modelTitle), new ProjectDeadline(modelDeadline),
                    ProjectStatus.fromString(modelStatus), ProjectPriority.fromString(modelPriority));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
    }

    private static String readText(JsonNode field, String name) throws IllegalValueException {
        requireField(field, name);
        if (!field.isTextual()) {
            throw new IllegalValueException("Project's " + name + " field must be a string.");
        }
        return field.textValue();
    }

    private static void requireField(JsonNode field, String name) throws IllegalValueException {
        if (field == null || field.isNull()) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, name));
        }
    }
}
