package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

public class JsonAdaptedPersonProjectsTest {

    private static final Project PROJECT = new Project(7, new ProjectTitle("Website redesign"),
            new ProjectDeadline("2026-10-15"), ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);
    private static final Person PERSON = ALICE.withProjects(List.of(PROJECT), 10);

    @Test
    public void toModelType_legacyFile_defaultsToEmptyProjects() throws Exception {
        ObjectNode legacy = personJson(ALICE);
        legacy.remove("projects");
        legacy.remove("nextProjectId");
        assertEquals(ALICE, read(legacy).toModelType());
    }

    @Test
    public void toModelType_missingCounter_usesMaximumIdPlusOne() throws Exception {
        ObjectNode legacy = personJson(PERSON);
        legacy.remove("nextProjectId");
        Person loaded = read(legacy).toModelType();
        assertEquals(List.of(PROJECT), loaded.getProjects());
        assertEquals(8, loaded.getNextProjectId());
        Project finalProject = new Project(Integer.MAX_VALUE, PROJECT.getTitle(), PROJECT.getDeadline(),
                ProjectStatus.COMPLETED, ProjectPriority.LOW);
        ObjectNode exhausted = personJson(ALICE.withProjects(List.of(finalProject), Person.MAX_NEXT_PROJECT_ID));
        exhausted.remove("nextProjectId");
        assertEquals(Person.MAX_NEXT_PROJECT_ID, read(exhausted).toModelType().getNextProjectId());
    }

    @Test
    public void toModelType_roundTrip_preservesProjectsAndRetiredIds() throws Exception {
        assertEquals(PERSON, read(personJson(PERSON)).toModelType());
        Person afterDeletion = PERSON.withProjects(List.of(), 10);
        assertEquals(afterDeletion, read(personJson(afterDeletion)).toModelType());
        Person exhausted = ALICE.withProjects(List.of(), Person.MAX_NEXT_PROJECT_ID);
        assertEquals(exhausted, read(personJson(exhausted)).toModelType());
    }

    @Test
    public void serialization_projects_remainsArrayWithoutValidationMetadata() throws Exception {
        ObjectNode json = personJson(PERSON);
        assertEquals(8, json.size());
        assertTrue(json.get("projects").isArray());
        assertEquals(1, json.get("projects").size());
        assertFalse(json.has("hasInvalidProjects"));
        assertFalse(json.has("invalidProjects"));
        assertEquals(json, JsonUtil.fromJsonString(JsonUtil.toJsonString(read(json)), ObjectNode.class));
        assertTrue(personJson(ALICE).get("projects").isArray());
        assertEquals(0, personJson(ALICE).get("projects").size());
    }

    @Test
    public void toModelType_nullOrNonArrayProjects_throwsIllegalValueException() throws Exception {
        String[] invalidProjects = {"null", "{}", "{\"id\":7}", "0", "1.5", "\"projects\"", "true"};
        for (String projects : invalidProjects) {
            ObjectNode json = personJson(PERSON);
            json.set("projects", JsonUtil.fromJsonString(projects, JsonNode.class));
            assertInvalid(json);
        }
    }

    @Test
    public void toModelType_nonObjectProjectEntry_throwsIllegalValueException() throws Exception {
        String[] invalidEntries = {"null", "[]", "0", "1.5", "\"project\"", "true"};
        for (String entry : invalidEntries) {
            ObjectNode json = personJson(PERSON);
            ArrayNode projects = (ArrayNode) json.get("projects");
            projects.add(JsonUtil.fromJsonString(entry, JsonNode.class));
            assertInvalid(json);
        }
    }

    @Test
    public void toModelType_nestedProjectMissingField_throwsIllegalValueException() throws Exception {
        for (String field : List.of("id", "title", "deadline", "status", "priority")) {
            ObjectNode json = personJson(PERSON);
            ArrayNode projects = (ArrayNode) json.get("projects");
            ObjectNode project = (ObjectNode) projects.get(0);
            project.remove(field);
            assertInvalid(json);
            project.putNull(field);
            assertInvalid(json);
        }
    }

    @Test
    public void toModelType_duplicateIds_throwsIllegalValueException() throws Exception {
        ObjectNode json = personJson(PERSON);
        ArrayNode projects = (ArrayNode) json.get("projects");
        ObjectNode duplicate = ((ObjectNode) projects.get(0)).deepCopy();
        duplicate.put("title", "Another project");
        projects.add(duplicate);
        assertInvalid(json);
    }

    @Test
    public void toModelType_duplicateTitleAndDeadline_throwsIllegalValueException() throws Exception {
        ObjectNode json = personJson(PERSON);
        ArrayNode projects = (ArrayNode) json.get("projects");
        ObjectNode duplicate = ((ObjectNode) projects.get(0)).deepCopy();
        duplicate.put("id", 8);
        duplicate.put("title", " WEBSITE\u00a0  REDESIGN ");
        duplicate.put("status", "completed");
        duplicate.put("priority", "low");
        projects.add(duplicate);
        assertInvalid(json);
    }

    @Test
    public void toModelType_nullProject_throwsIllegalValueException() throws Exception {
        ObjectNode json = personJson(PERSON);
        ArrayNode projects = (ArrayNode) json.get("projects");
        projects.addNull();
        assertInvalid(json);
    }

    @Test
    public void toModelType_invalidCounter_throwsIllegalValueException() throws Exception {
        String[] counters = {"null", "0", "-1", "6", "7", "2147483649", "9223372036854775808", "8.5",
            "8.0", "8e0", "\"8\"", "true", "[]", "{}"};
        for (String counter : counters) {
            ObjectNode json = personJson(PERSON);
            json.set("nextProjectId", JsonUtil.fromJsonString(counter, JsonNode.class));
            assertInvalid(json);
        }
    }

    @Test
    public void toModelType_nullTag_throwsIllegalValueException() throws Exception {
        ObjectNode json = personJson(PERSON);
        ArrayNode tags = (ArrayNode) json.get("tags");
        tags.addNull();
        assertInvalid(json);
    }

    @Test
    public void addressBookRoundTrip_sameProjectForDifferentClients_preservesBoth() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(PERSON);
        addressBook.addPerson(BOB.withProjects(List.of(PROJECT), 8));
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        AddressBook loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(addressBook, loaded);
    }

    private static ObjectNode personJson(Person person) throws Exception {
        return JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonAdaptedPerson(person)), ObjectNode.class);
    }

    private static JsonAdaptedPerson read(ObjectNode json) throws Exception {
        return JsonUtil.fromJsonString(json.toString(), JsonAdaptedPerson.class);
    }

    private static void assertInvalid(ObjectNode json) throws Exception {
        JsonAdaptedPerson person = read(json);
        assertThrows(IllegalValueException.class, person::toModelType);
    }
}
