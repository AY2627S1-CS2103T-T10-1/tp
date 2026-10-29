package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddProjectCommand;
import seedu.address.logic.commands.EditProjectCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Verifies PR #58's project creation together with PR #57's project editing.
 */
public class AddEditProjectIntegrationTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void execute_addEditReloadAndAdd_retainsWorkIdsAndCounter() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        Logic logic = createLogic(model, storage);
        logic.execute("add-project 1 title/Website deadline/2026-10-15");
        logic.execute("edit-project 1 1 status/completed priority/high");
        model = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        logic = createLogic(model, storage);
        logic.execute("add-project 1 title/Logo deadline/2026-10-15");
        logic.execute("edit-project 1 1 status/in-progress");
        Person client = model.getFilteredPersonList().getFirst();
        assertEquals(3, client.getNextProjectId());
        assertEquals(1, client.getProjects().getFirst().getId());
        assertEquals(ProjectStatus.IN_PROGRESS, client.getProjects().getFirst().getStatus());
        assertEquals(ProjectPriority.HIGH, client.getProjects().getFirst().getPriority());
        assertEquals(2, client.getProjects().get(1).getId());
        Logic finalLogic = logic;
        assertThrows(CommandException.class, () -> finalLogic.execute("edit-project 1 1 title/Logo"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_unicodeCaseDuplicates_rejectsAddAndEditWithoutChangingLiveOrSavedData() throws Exception {
        String[][] titles = {{"IşıkWebsite", "IŞIKWEBSITE"}, {"İ Website", "i WEBSITE"},
            {"İ Website", "i\u0307 WEBSITE"}, {"Σ Website", "ς WEBSITE"},
            {"Straße", "STRAẞE"}, {"𐐀 Website", "𐐨 WEBSITE"}};
        for (String[] pair : titles) {
            Model model = new ModelManager();
            model.addPerson(ALICE);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
            Logic logic = createLogic(model, storage);
            logic.execute("add-project 1 title/" + pair[0] + " deadline/2026-10-15");
            logic.execute("add-project 1 title/Other work deadline/2026-10-15");

            assertRejectedWithoutChanges(logic, model, storage,
                    "add-project 1 title/" + pair[1] + " deadline/2026-10-15 status/in-progress priority/high",
                    String.format(AddProjectCommand.MESSAGE_DUPLICATE_PROJECT, ALICE.getName()));
            assertRejectedWithoutChanges(logic, model, storage,
                    "edit-project 1 2 title/" + pair[1] + " priority/high", EditProjectCommand.MESSAGE_DUPLICATE);

            logic.execute("add-project 1 title/" + pair[1] + " deadline/2026-10-16");
            Person client = model.getFilteredPersonList().getFirst();
            assertEquals(List.of(1, 2, 3), client.getProjects().stream().map(Project::getId).toList());
            assertEquals(4, client.getNextProjectId());
            assertEquals(pair[0], client.getProjects().getFirst().getTitle().value);
            assertEquals(pair[1], client.getProjects().get(2).getTitle().value);
            assertRejectedWithoutChanges(logic, model, storage,
                    "edit-project 1 3 deadline/2026-10-15", EditProjectCommand.MESSAGE_DUPLICATE);
        }
    }

    @Test
    public void execute_expandingCaseMapping_preservesDistinctProjectsOnReload() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        Logic logic = createLogic(model, storage);
        logic.execute("add-project 1 title/Straße deadline/2026-10-15");
        logic.execute("add-project 1 title/STRASSE deadline/2026-10-15");
        Person client = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(model.getFilteredPersonList().getFirst(), client);
        assertEquals(List.of("Straße", "STRASSE"),
                client.getProjects().stream().map(project -> project.getTitle().value).toList());
        assertEquals(List.of(1, 2), client.getProjects().stream().map(Project::getId).toList());
        assertEquals(3, client.getNextProjectId());
    }

    @Test
    public void execute_legacyUnicodeVariants_preservesDataAcrossLoadUpdateDelete() throws Exception {
        Path path = temporaryFolder.resolve("clients.json");
        Files.writeString(path, """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[],"projects":[
                {"id":1,"title":"IşıkWebsite","deadline":"2026-10-15",
                "status":"not-started","priority":"medium"},
                {"id":2,"title":"IŞIKWEBSITE","deadline":"2026-10-15",
                "status":"in-progress","priority":"low"}],"nextProjectId":3}]}
                """);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        Model model = new ModelManager();
        model.setAddressBook(storage.readAddressBook().orElseThrow());
        Person original = model.getFilteredPersonList().getFirst();
        assertEquals(List.of("IşıkWebsite", "IŞIKWEBSITE"),
                original.getProjects().stream().map(project -> project.getTitle().value).toList());
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        Logic logic = createLogic(model, storage);
        logic.execute("edit-project 1 1 title/IşıkWebsite deadline/2026-10-15 status/completed priority/high");
        Person updated = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        Project first = updated.getProjects().getFirst();
        assertEquals(1, first.getId());
        assertEquals(original.getProjects().getFirst().getTitle(), first.getTitle());
        assertEquals(original.getProjects().getFirst().getDeadline(), first.getDeadline());
        assertEquals(ProjectStatus.COMPLETED, first.getStatus());
        assertEquals(ProjectPriority.HIGH, first.getPriority());
        assertEquals(original.getProjects().get(1), updated.getProjects().get(1));
        assertEquals(3, updated.getNextProjectId());
        assertRejectedWithoutChanges(logic, model, storage,
                "edit-project 1 1 title/IŞIKWEBSITE", EditProjectCommand.MESSAGE_DUPLICATE);

        logic.execute("delete-project 1 2");
        Person afterDeletion = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(List.of(first), afterDeletion.getProjects());
        assertEquals(3, afterDeletion.getNextProjectId());
        logic.execute("add-project 1 title/Other work deadline/2026-10-15");
        Person afterAddition = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(List.of(1, 3), afterAddition.getProjects().stream().map(Project::getId).toList());
        assertEquals(4, afterAddition.getNextProjectId());
    }

    private void assertRejectedWithoutChanges(Logic logic, Model model, JsonAddressBookStorage storage,
            String command, String message) throws Exception {
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        assertEquals(message, assertThrows(CommandException.class, () -> logic.execute(command)).getMessage());
        assertEquals(original, model.getAddressBook().getPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }
}
