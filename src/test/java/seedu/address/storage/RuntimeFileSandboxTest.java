package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class RuntimeFileSandboxTest {
    @TempDir
    Path temporaryFolder;

    @Test
    public void storage_homeReplacedByExternalLink_keepsOriginalBoundary() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path outside = Files.createDirectory(temporaryFolder.resolve("outside"));
        JsonAddressBookStorage data = new JsonAddressBookStorage(home.resolve("addressbook.json"), home);
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(home.resolve("preferences.json"), home);
        Files.writeString(outside.resolve("addressbook.json"), "{\"persons\":[]}\n");
        Files.writeString(outside.resolve("preferences.json"), "{}\n");
        Files.move(home, temporaryFolder.resolve("original-home"));
        link(home, outside);

        assertThrows(DataLoadingException.class, data::readAddressBook);
        assertThrows(IOException.class, () -> data.saveAddressBook(getTypicalAddressBook()));
        assertThrows(DataLoadingException.class, prefs::readUserPrefs);
        assertThrows(IOException.class, () -> prefs.saveUserPrefs(new UserPrefs()));

        assertEquals("{\"persons\":[]}\n", Files.readString(outside.resolve("addressbook.json")));
        assertEquals("{}\n", Files.readString(outside.resolve("preferences.json")));
    }

    @Test
    public void saveAddressBook_externalParentWithInternalLeafLinks_rejectsTemporaryWrite() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path outside = Files.createDirectory(temporaryFolder.resolve("outside"));
        Path safeData = home.resolve("safe.json");
        Path safeLock = Files.createFile(home.resolve("safe.lock"));
        Files.writeString(safeData, "{\"persons\":[]}\n");
        link(home.resolve("data"), outside);
        link(outside.resolve("addressbook.json"), safeData);
        link(outside.resolve("addressbook.json.lock"), safeLock);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(home.resolve("data/addressbook.json"), home);

        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));

        assertTrue(Files.isSymbolicLink(outside.resolve("addressbook.json")));
        assertEquals("{\"persons\":[]}\n", Files.readString(safeData));
        try (var entries = Files.list(outside)) {
            assertEquals(2, entries.count());
        }
    }

    @Test
    public void addressBook_externalDirectoryOrFileLink_rejectsReadAndSave() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path outside = Files.createDirectory(temporaryFolder.resolve("outside"));
        Path externalFile = outside.resolve("addressbook.json");
        Files.writeString(externalFile, "{\"persons\":[]}\n");
        link(home.resolve("data"), outside);
        link(home.resolve("linked.json"), externalFile);
        for (Path path : List.of(home.resolve("data/addressbook.json"), home.resolve("linked.json"))) {
            JsonAddressBookStorage storage = new JsonAddressBookStorage(path, home);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));
        }
        assertEquals("{\"persons\":[]}\n", Files.readString(externalFile));
        assertFalse(Files.exists(outside.resolve("addressbook.json.lock")));
    }

    @Test
    public void addressBook_linkChangedAfterLoad_rejectsWithoutPublishingOrWriting() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path data = Files.createDirectory(home.resolve("data"));
        Path outside = Files.createDirectory(temporaryFolder.resolve("outside"));
        Path externalFile = outside.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(data.resolve("addressbook.json"), home);
        AddressBook original = getTypicalAddressBook();
        storage.saveAddressBook(original);
        ModelManager model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager logic = new LogicManager(model,
                new StorageManager(storage, new JsonUserPrefsStorage(home.resolve("preferences.json"), home)));
        logic.execute("find Alice");
        List<Person> displayedClients = List.copyOf(model.getFilteredPersonList());
        Files.writeString(externalFile, "{\"persons\":[]}\n");
        Files.move(data, home.resolve("saved-data"));
        link(data, outside);

        assertThrows(CommandException.class, () -> logic.execute("clear"));

        assertEquals(original, model.getAddressBook());
        assertEquals(displayedClients, model.getFilteredPersonList());
        assertEquals("{\"persons\":[]}\n", Files.readString(externalFile));
        assertFalse(Files.exists(outside.resolve("addressbook.json.lock")));
        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void addressBook_externalLockLink_rejectsBeforeOpeningLock() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path missingLockTarget = temporaryFolder.resolve("outside-lock");
        link(home.resolve("addressbook.json.lock"), missingLockTarget);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(home.resolve("addressbook.json"), home);

        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));

        assertFalse(Files.exists(missingLockTarget));
        assertFalse(Files.exists(home.resolve("addressbook.json")));
    }

    @Test
    public void preferences_externalLink_rejectsReadAndWriteWithoutChangingTarget() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path outside = temporaryFolder.resolve("outside-prefs.json");
        Files.writeString(outside, "{}\n");
        Path path = home.resolve("preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(path, home);
        storage.saveUserPrefs(new UserPrefs());
        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
        Files.delete(path);
        link(path, outside);

        assertThrows(DataLoadingException.class, storage::readUserPrefs);
        assertThrows(IOException.class, () -> storage.saveUserPrefs(new UserPrefs()));

        assertEquals("{}\n", Files.readString(outside));
    }

    @Test
    public void addressBook_missingExternalTarget_rejectsWithoutCreatingFiles() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path outside = Files.createDirectory(temporaryFolder.resolve("outside"));
        link(home.resolve("data"), outside);
        Path path = home.resolve("data/nested/addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path, home);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));

        assertFalse(Files.exists(outside.resolve("nested")));
    }

    @Test
    public void storage_missingPathsAndInternalLinks_roundTripNormally() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path internal = Files.createDirectory(home.resolve("internal"));
        link(home.resolve("data"), internal);
        JsonAddressBookStorage data = new JsonAddressBookStorage(home.resolve("data/addressbook.json"), home);
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(home.resolve("preferences.json"), home);
        assertTrue(data.readAddressBook().isEmpty());
        assertTrue(prefs.readUserPrefs().isEmpty());
        data.saveAddressBook(getTypicalAddressBook());
        prefs.saveUserPrefs(new UserPrefs());
        assertEquals(getTypicalAddressBook(), data.readAddressBook().orElseThrow());
        assertEquals(new UserPrefs(), prefs.readUserPrefs().orElseThrow());
        link(home.resolve("preferences-link.json"), home.resolve("preferences.json"));
        JsonUserPrefsStorage linkedPrefs = new JsonUserPrefsStorage(home.resolve("preferences-link.json"), home);
        linkedPrefs.saveUserPrefs(new UserPrefs());
        assertEquals(new UserPrefs(), linkedPrefs.readUserPrefs().orElseThrow());
    }

    private static void link(Path link, Path target) throws IOException {
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | FileSystemException exception) {
            assumeTrue(false, "Symbolic links are unavailable: " + exception);
        }
    }
}
