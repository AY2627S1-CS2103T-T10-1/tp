package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

public class JsonAdaptedProjectTest {

    private static final Project PROJECT = new Project(7, new ProjectTitle("Website  redesign 🎨"),
            new ProjectDeadline("2026-10-15"), ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);

    @Test
    public void toModelType_validProject_roundTripsAllFields() throws Exception {
        assertEquals(PROJECT, new JsonAdaptedProject(PROJECT).toModelType());
        assertEquals(PROJECT, read(projectJson()).toModelType());
        Project completed = new Project(Integer.MAX_VALUE, PROJECT.getTitle(), PROJECT.getDeadline(),
                ProjectStatus.COMPLETED, ProjectPriority.LOW);
        String json = JsonUtil.toJsonString(new JsonAdaptedProject(completed));
        assertEquals(completed, JsonUtil.fromJsonString(json, JsonAdaptedProject.class).toModelType());
    }

    @Test
    public void toModelType_missingOrNullField_throwsIllegalValueException() throws Exception {
        String[] fields = {"id", "title", "deadline", "status", "priority"};
        for (String field : fields) {
            ObjectNode missing = projectJson();
            missing.remove(field);
            assertInvalid(missing);
            ObjectNode nullField = projectJson();
            nullField.putNull(field);
            assertInvalid(nullField);
        }
    }

    @Test
    public void toModelType_invalidId_throwsIllegalValueException() throws Exception {
        String[] invalidIds = {"0", "-1", "2147483648", "9223372036854775808", "1.5", "1.0", "1e0",
            "\"1\"", "true", "[]", "{}"};
        for (String invalidId : invalidIds) {
            ObjectNode json = projectJson();
            json.set("id", JsonUtil.fromJsonString(invalidId, JsonNode.class));
            assertInvalid(json);
        }
    }

    @Test
    public void toModelType_nonStringField_throwsIllegalValueException() throws Exception {
        String[] fields = {"title", "deadline", "status", "priority"};
        String[] invalidValues = {"123", "1.5", "true", "[]", "{}"};
        for (String field : fields) {
            for (String value : invalidValues) {
                ObjectNode json = projectJson();
                json.set(field, JsonUtil.fromJsonString(value, JsonNode.class));
                assertInvalid(json);
            }
        }
    }

    @Test
    public void toModelType_invalidFieldValue_throwsIllegalValueException() throws Exception {
        ObjectNode title = projectJson();
        title.put("title", "Project\nname");
        assertInvalid(title);
        ObjectNode deadline = projectJson();
        deadline.put("deadline", "2026-02-29");
        assertInvalid(deadline);
        ObjectNode status = projectJson();
        status.put("status", "done");
        assertInvalid(status);
        ObjectNode priority = projectJson();
        priority.put("priority", "urgent");
        assertInvalid(priority);
    }

    private static ObjectNode projectJson() throws Exception {
        return JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonAdaptedProject(PROJECT)), ObjectNode.class);
    }

    private static JsonAdaptedProject read(ObjectNode json) throws Exception {
        return JsonUtil.fromJsonString(json.toString(), JsonAdaptedProject.class);
    }

    private static void assertInvalid(ObjectNode json) throws Exception {
        JsonAdaptedProject project = read(json);
        assertThrows(IllegalValueException.class, project::toModelType);
    }
}
