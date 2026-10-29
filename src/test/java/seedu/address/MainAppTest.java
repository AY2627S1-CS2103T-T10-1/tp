package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;

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
        AtomicInteger uiStarts = new AtomicInteger();
        app.ui = stage -> uiStarts.incrementAndGet();
        app.start(null);
        assertEquals(1, uiStarts.get());
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
        AtomicInteger uiStarts = new AtomicInteger();
        app.ui = stage -> uiStarts.incrementAndGet();
        app.start(null);
        assertEquals(1, uiStarts.get());
        assertFalse(app.errorShown);
        app.stop();
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void init_malformedJson_blocksCommandsAndPreservesBytes() throws Exception {
        assertFailedStartupPreservesFile("malformed.json", "{\"persons\": [");
    }

    @Test
    public void init_malformedUtf8_blocksCommandsAndPreservesBytes() throws Exception {
        String contents = """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"café","tags":[]}]}
                """;
        assertFailedStartupPreservesFile("malformed-utf8.json", contents.getBytes(StandardCharsets.ISO_8859_1));
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
    public void init_nullGuiSettings_usesDefaultsWithoutChangingClientData() throws Exception {
        Path dataFile = temporaryFolder.resolve("clients.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataFile);
        addressBookStorage.saveAddressBook(getTypicalAddressBook());
        byte[] originalClientData = Files.readAllBytes(dataFile);
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Files.writeString(prefsFile, "{\"guiSettings\":null}");
        PreferencesTestMainApp app = new PreferencesTestMainApp(dataFile, prefsFile);

        app.init();

        assertEquals(getTypicalAddressBook(), app.model.getAddressBook());
        assertEquals(new GuiSettings(), app.model.getGuiSettings());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertArrayEquals(originalClientData, Files.readAllBytes(dataFile));
        assertEquals(new GuiSettings(), new JsonUserPrefsStorage(prefsFile)
                .readUserPrefs().orElseThrow().getGuiSettings());
    }

    @Test
    public void init_validGuiSettings_preservesSavedValuesAndClientData() throws Exception {
        Path dataFile = temporaryFolder.resolve("clients.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataFile);
        addressBookStorage.saveAddressBook(getTypicalAddressBook());
        byte[] originalClientData = Files.readAllBytes(dataFile);
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        GuiSettings savedSettings = new GuiSettings(1111, 777, 12, 34);
        UserPrefs savedPrefs = new UserPrefs();
        savedPrefs.setGuiSettings(savedSettings);
        new JsonUserPrefsStorage(prefsFile).saveUserPrefs(savedPrefs);
        PreferencesTestMainApp app = new PreferencesTestMainApp(dataFile, prefsFile);

        app.init();

        assertEquals(savedSettings, app.model.getGuiSettings());
        assertArrayEquals(originalClientData, Files.readAllBytes(dataFile));
        assertEquals(savedSettings, new JsonUserPrefsStorage(prefsFile)
                .readUserPrefs().orElseThrow().getGuiSettings());
    }

    @Test
    public void init_missingGuiSettings_usesDefaults() throws Exception {
        Path dataFile = temporaryFolder.resolve("clients.json");
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Files.writeString(prefsFile, "{}");
        PreferencesTestMainApp app = new PreferencesTestMainApp(dataFile, prefsFile);

        app.init();

        assertEquals(new GuiSettings(), app.model.getGuiSettings());
        assertNotNull(app.logic);

        Path emptySettingsFile = temporaryFolder.resolve("empty-settings.json");
        Files.writeString(emptySettingsFile, "{\"guiSettings\":{}}");
        PreferencesTestMainApp emptySettingsApp = new PreferencesTestMainApp(dataFile, emptySettingsFile);
        emptySettingsApp.init();

        assertEquals(new GuiSettings(), emptySettingsApp.model.getGuiSettings());
        assertNotNull(emptySettingsApp.logic);
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

    @Test
    public void init_brokenDataSymlink_blocksCommandsAndPreservesLink() throws Exception {
        Path missingTarget = temporaryFolder.resolve("missing-target.json");
        Path dataPath = temporaryFolder.resolve("broken-link.json");
        try {
            Files.createSymbolicLink(dataPath, missingTarget);
        } catch (UnsupportedOperationException | FileSystemException e) {
            assumeTrue(false, "Symbolic links are unavailable: " + e);
        }
        TestMainApp app = new TestMainApp(dataPath);

        app.init();
        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        app.start(null);
        assertTrue(app.errorShown);
        assertEquals(dataPath, app.errorPath);
        app.stop();

        assertTrue(Files.isSymbolicLink(dataPath));
        assertEquals(missingTarget, Files.readSymbolicLink(dataPath));
        assertFalse(Files.exists(missingTarget));
    }

    private void assertFailedStartupPreservesFile(String fileName, String contents) throws Exception {
        assertFailedStartupPreservesFile(fileName, contents.getBytes(StandardCharsets.UTF_8));
    }

    private void assertFailedStartupPreservesFile(String fileName, byte[] original) throws Exception {
        Path dataFile = temporaryFolder.resolve(fileName);
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
            return super.createStorage(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(dataFile.getParent().resolve("preferences.json")));
        }

        @Override
        protected void showDataLoadingError(Path dataFilePath) {
            errorShown = true;
            errorPath = dataFilePath;
        }
    }

    private static class PreferencesTestMainApp extends MainApp {
        private final Path dataFile;
        private final Path prefsFile;

        PreferencesTestMainApp(Path dataFile, Path prefsFile) {
            this.dataFile = dataFile;
            this.prefsFile = prefsFile;
        }

        @Override
        protected UserPrefs initPrefs(JsonUserPrefsStorage ignored) {
            return super.initPrefs(new JsonUserPrefsStorage(prefsFile));
        }

        @Override
        protected Storage createStorage(JsonAddressBookStorage ignoredData,
                JsonUserPrefsStorage ignoredPrefs) {
            return super.createStorage(new JsonAddressBookStorage(dataFile), new JsonUserPrefsStorage(prefsFile));
        }
    }
}
