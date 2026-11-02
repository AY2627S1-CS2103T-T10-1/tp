package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Verifies canonical search matching without changing the spelling or saved client records. */
public class CanonicalFindIntegrationTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_equivalentAccents_findsBothSpellingsWithoutSaving() throws Exception {
        var composed = new PersonBuilder().withName("José").withPhone("81234567")
                .withEmail("composed@example.com").build();
        var decomposed = new PersonBuilder().withName("Jose\u0301").withPhone("92345678")
                .withEmail("decomposed@example.com").build();
        var unrelated = new PersonBuilder().withName("Beacon").withPhone("93456789")
                .withEmail("beacon@example.com").build();
        ModelManager model = new ModelManager();
        List.of(composed, decomposed, unrelated).forEach(model::addPerson);
        Path dataFile = testFolder.resolve("clients.json");
        var storage = new JsonAddressBookStorage(dataFile);
        storage.saveAddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(dataFile);
        var logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        for (String query : List.of("José", "Jose\u0301", "JOSÉ", "é", "e\u0301")) {
            assertEquals("Found 2 matching clients.", logic.execute("find " + query).getFeedbackToUser());
            assertEquals(2, model.getFilteredPersonList().size());
            assertEquals(composed, logic.execute("view 2").getClientToSelect().orElseThrow());
            assertArrayEquals(saved, Files.readAllBytes(dataFile));
        }
        assertEquals("Found 1 matching clients.",
                logic.execute("find Jose\u0301 decomposed@").getFeedbackToUser());
        assertEquals(List.of(decomposed), model.getFilteredPersonList());
        assertEquals("No clients matched: Jose.", logic.execute("find Jose").getFeedbackToUser());
        assertEquals(3, model.getFilteredPersonList().size());
        assertEquals(List.of(composed, decomposed, unrelated), model.getAddressBook().getPersonList());
        assertArrayEquals(saved, Files.readAllBytes(dataFile));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
