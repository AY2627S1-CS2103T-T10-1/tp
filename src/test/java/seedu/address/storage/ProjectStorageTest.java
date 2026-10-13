package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;

/**
 * Tests schema compatibility, project validation and immutable snapshot data.
 */
public class ProjectStorageTest {
    private static final Project PROJECT = new Project(11, new ProjectTitle("Website"),
            new ProjectDeadline("2026-10-15"),
            ProjectStatus.COMPLETED, ProjectPriority.HIGH);

    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndLoad_projectsAndIds_roundTrip() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        AddressBook book = new AddressBook();
        book.addPerson(ALICE.withProjects(List.of(PROJECT), 12));
        storage.saveAddressBook(book);
        assertEquals(book, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void read_legacyClientWithoutProjects_loadsEmptyProjects() throws Exception {
        Path path = temporaryFolder.resolve("legacy.json");
        Files.writeString(path, """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[]}]}
                """);
        Person loaded = new JsonAddressBookStorage(path).readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(List.of(), loaded.getProjects());
        assertEquals("Alice", loaded.getName().fullName);
    }

    @Test
    public void toModelType_invalidProjectFields_reportIllegalValue() {
        List<JsonAdaptedProject> invalid = Arrays.asList(
                adaptedProject(null, "Website", "2026-10-15", "completed", "high"),
                adaptedProject(0, "Website", "2026-10-15", "completed", "high"),
                adaptedProject(1, null, "2026-10-15", "completed", "high"),
                adaptedProject(1, " ", "2026-10-15", "completed", "high"),
                adaptedProject(1, "Website", null, "completed", "high"),
                adaptedProject(1, "Website", "2026-02-29", "completed", "high"),
                adaptedProject(1, "Website", "2026-10-15", null, "high"),
                adaptedProject(1, "Website", "2026-10-15", "pending", "high"),
                adaptedProject(1, "Website", "2026-10-15", "completed", null),
                adaptedProject(1, "Website", "2026-10-15", "completed", "urgent"));
        for (JsonAdaptedProject project : invalid) {
            assertThrows(IllegalValueException.class, project::toModelType);
        }
    }

    @Test
    public void toModelType_duplicateIdsOrNullProjects_rejects() {
        List<JsonAdaptedProject> duplicateIds = List.of(
                new JsonAdaptedProject(PROJECT), new JsonAdaptedProject(PROJECT));
        assertThrows(IllegalValueException.class, () -> adaptedClient(duplicateIds).toModelType());
        List<JsonAdaptedProject> nullProject = Arrays.asList((JsonAdaptedProject) null);
        assertThrows(IllegalValueException.class, () -> adaptedClient(nullProject).toModelType());
    }

    @Test
    public void read_invalidProject_doesNotOverwriteFile() throws Exception {
        Path path = temporaryFolder.resolve("invalid.json");
        String json = """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[],"projects":[
                {"id":11,"title":"Website","deadline":"2026-02-29","status":"completed","priority":"high"}]}]}
                """;
        Files.writeString(path, json);
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook());
        assertEquals(json, Files.readString(path));
    }

    @Test
    public void personProjects_defensiveCopyAndEquality_includeProjectData() {
        List<Project> projects = new ArrayList<>(List.of(PROJECT));
        Person client = ALICE.withProjects(projects, 12);
        projects.clear();
        assertEquals(List.of(PROJECT), client.getProjects());
        assertThrows(UnsupportedOperationException.class, () -> client.getProjects().clear());
        assertThrows(IllegalArgumentException.class, () -> ALICE.withProjects(List.of(PROJECT, PROJECT), 12));
        assertNotEquals(ALICE, client);
        assertEquals(client, ALICE.withProjects(List.of(PROJECT), 12));
        assertEquals(client.hashCode(), ALICE.withProjects(List.of(PROJECT), 12).hashCode());
    }

    private JsonAdaptedPerson adaptedClient(List<JsonAdaptedProject> projects) throws Exception {
        ObjectNode client = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE)),
                ObjectNode.class);
        client.set("projects", JsonUtil.fromJsonString(JsonUtil.toJsonString(projects), JsonNode.class));
        client.put("nextProjectId", 12);
        return JsonUtil.fromJsonString(client.toString(), JsonAdaptedPerson.class);
    }

    private JsonAdaptedProject adaptedProject(Integer id, String title, String deadline, String status,
            String priority) {
        return new JsonAdaptedProject(id == null ? null : IntNode.valueOf(id), text(title), text(deadline),
                text(status), text(priority));
    }

    private JsonNode text(String value) {
        return value == null ? null : TextNode.valueOf(value);
    }
}
