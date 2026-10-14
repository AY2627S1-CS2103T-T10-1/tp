package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
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

    private Logic createLogic(Model model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }
}
