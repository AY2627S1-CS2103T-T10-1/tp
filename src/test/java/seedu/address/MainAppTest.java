package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

/**
 * Tests application startup when client data can or cannot be loaded.
 */
public class MainAppTest {

    @TempDir
    Path temporaryFolder;

    @Test
    public void stop_beforeInitialization_doesNotAccessMissingModel() {
        TestMainApp app = new TestMainApp(temporaryFolder.resolve("not-started.json"));

        app.stop();

        assertNull(app.model);
    }

    @Test
    public void init_validData_startsWithStoredClients() throws Exception {
        Path dataFile = temporaryFolder.resolve("valid.json");
        new JsonAddressBookStorage(dataFile).saveAddressBook(getTypicalAddressBook());
        TestMainApp app = new TestMainApp(dataFile);

        app.init();

        assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertFalse(app.errorShown);
        app.stop();
    }

    @Test
    public void init_missingData_startsWithSampleWithoutWritingDataFile() throws Exception {
        Path dataFile = temporaryFolder.resolve("missing.json");
        TestMainApp app = new TestMainApp(dataFile);

        app.init();

        assertNotNull(app.model);
        assertFalse(app.model.getAddressBook().getPersonList().isEmpty());
        assertNotNull(app.logic);
        assertFalse(Files.exists(dataFile));
        app.stop();
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void init_malformedJson_blocksCommandsAndPreservesBytes() throws Exception {
        assertFailedStartupPreservesFile("malformed.json", "{\"persons\": [");
    }

    @Test
    public void init_invalidStoredValue_blocksCommandsAndPreservesBytes() throws Exception {
        assertFailedStartupPreservesFile("invalid.json", """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[],"projects":[
                {"id":1,"title":"Website","deadline":"2026-02-29","status":"completed","priority":"high"}],
                "nextProjectId":2}]}
                """);
    }

    @Test
    public void init_nullJsonRoot_blocksCommandsAndPreservesBytes() throws Exception {
        assertFailedStartupPreservesFile("null-root.json", "null");
    }

    @Test
    public void init_nullPerson_blocksCommandsAndPreservesBytes() throws Exception {
        assertFailedStartupPreservesFile("null-person.json", "{\"persons\":[null]}");
    }

    @Test
    public void init_nullTag_blocksCommandsAndPreservesBytes() throws Exception {
        assertFailedStartupPreservesFile("null-tag.json", """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[null]}]}
                """);
    }

    @Test
    public void init_readFailure_blocksCommandsAndLeavesDirectoryUntouched() throws Exception {
        Path dataPath = Files.createDirectory(temporaryFolder.resolve("unreadable.json"));
        Path sentinel = dataPath.resolve("keep.txt");
        Files.writeString(sentinel, "Existing data");
        TestMainApp app = new TestMainApp(dataPath);

        app.init();
        app.start(null);
        app.stop();

        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        assertTrue(app.errorShown);
        assertEquals(dataPath, app.errorPath);
        assertEquals("Existing data", Files.readString(sentinel));
    }

    private void assertFailedStartupPreservesFile(String fileName, String contents) throws Exception {
        Path dataFile = temporaryFolder.resolve(fileName);
        byte[] original = contents.getBytes(StandardCharsets.UTF_8);
        Files.write(dataFile, original);
        TestMainApp app = new TestMainApp(dataFile);

        app.init();
        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        app.start(null);
        assertTrue(app.errorShown);
        assertEquals(dataFile, app.errorPath);
        app.stop();

        assertArrayEquals(original, Files.readAllBytes(dataFile));
    }

    private static class TestMainApp extends MainApp {
        private final Path dataFile;
        private boolean errorShown;
        private Path errorPath;

        TestMainApp(Path dataFile) {
            this.dataFile = dataFile;
        }

        @Override
        protected UserPrefs initPrefs(JsonUserPrefsStorage prefsStorage) {
            return new UserPrefs();
        }

        @Override
        protected Storage createStorage(JsonAddressBookStorage addressBookStorage,
                JsonUserPrefsStorage prefsStorage) {
            return new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(dataFile.getParent().resolve("preferences.json")));
        }

        @Override
        protected void showDataLoadingError(Path dataFilePath) {
            errorShown = true;
            errorPath = dataFilePath;
        }
    }
}
