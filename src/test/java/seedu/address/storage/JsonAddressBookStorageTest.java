package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void saveAddressBook_failedReplacement_preservesDestinationAndRemovesTemporaryFile() throws Exception {
        Path destination = Files.createDirectory(testFolder.resolve("existing-directory"));
        Path existing = destination.resolve("keep.txt");
        Files.writeString(existing, "Existing data");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));
        assertEquals("Existing data", Files.readString(existing));
        try (var files = Files.list(testFolder)) {
            assertEquals(List.of(destination, testFolder.resolve("existing-directory.lock")), files.sorted().toList());
        }
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void saveAddressBook_directoryBecomesReadOnly_preservesDataEvenIfCleanupFails() throws Exception {
        Path destination = testFolder.resolve("clients.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(destination);
        AddressBook original = getTypicalAddressBook();
        storage.saveAddressBook(original);
        String previousFile = Files.readString(destination);
        ReadOnlyAddressBook interruptedData = () -> {
            try {
                // Simulate directory permissions changing after the temporary file has been created.
                Files.setPosixFilePermissions(testFolder, PosixFilePermissions.fromString("r-x------"));
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
            return original.getPersonList();
        };
        try {
            assertThrows(IOException.class, () -> storage.saveAddressBook(interruptedData));
            assertEquals(previousFile, Files.readString(destination));
        } finally {
            Files.setPosixFilePermissions(testFolder, PosixFilePermissions.fromString("rwx------"));
        }
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void read_nullRootPersonOrTag_reportsDataLoadingException() throws Exception {
        List<String> invalidData = List.of("null", "{\"persons\":null}", "{\"persons\":[null]}", """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[null]}]}
                """);
        Path dataFile = testFolder.resolve("null-entry.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        for (String json : invalidData) {
            Files.writeString(dataFile, json);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(json, Files.readString(dataFile));
        }
    }

    @Test
    public void read_nonStringClientFields_preservesFileAndReportsLoadingFailure() throws Exception {
        List<String> invalidData = List.of("""
                {"persons":[{"name":"Alice","phone":81234567,"email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[]}]}
                """, """
                {"persons":[{"name":"Alice","phone":"81234567","email":"alice@example.com",
                "address":"Not provided","remark":"","tags":[123]}]}
                """);
        Path dataFile = testFolder.resolve("non-string-client-fields.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        for (String json : invalidData) {
            Files.writeString(dataFile, json);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(json, Files.readString(dataFile));
        }
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_staleReaderRejectedUntilReloaded() throws Exception {
        Path dataFile = testFolder.resolve("shared.json");
        new JsonAddressBookStorage(dataFile).saveAddressBook(getTypicalAddressBook());
        JsonAddressBookStorage first = new JsonAddressBookStorage(dataFile);
        JsonAddressBookStorage second = new JsonAddressBookStorage(dataFile);
        first.readAddressBook();
        second.readAddressBook();

        AddressBook firstData = new AddressBook();
        firstData.addPerson(ALICE);
        first.saveAddressBook(firstData);
        String firstSave = Files.readString(dataFile);

        AddressBook staleData = new AddressBook();
        staleData.addPerson(HOON);
        assertThrows(IOException.class, () -> second.saveAddressBook(staleData));
        assertEquals(firstSave, Files.readString(dataFile));

        second.readAddressBook();
        second.saveAddressBook(staleData);
        assertEquals(staleData, new AddressBook(second.readAddressBook().get()));
    }

    @Test
    public void saveAddressBook_staleMissingFileReaderRejectedUntilReloaded() throws Exception {
        Path dataFile = testFolder.resolve("initially-missing.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(dataFile);
        JsonAddressBookStorage second = new JsonAddressBookStorage(dataFile);
        assertFalse(first.readAddressBook().isPresent());
        assertFalse(second.readAddressBook().isPresent());

        AddressBook firstData = new AddressBook();
        firstData.addPerson(ALICE);
        first.saveAddressBook(firstData);

        AddressBook staleData = new AddressBook();
        staleData.addPerson(HOON);
        String firstSave = Files.readString(dataFile);
        assertThrows(IOException.class, () -> second.saveAddressBook(staleData));
        assertEquals(firstSave, Files.readString(dataFile));
        second.readAddressBook();
        second.saveAddressBook(staleData);
        assertEquals(staleData, new AddressBook(second.readAddressBook().get()));
    }

    @Test
    public void saveAddressBook_competingWriterCannotAcquireLock() throws Exception {
        Path dataFile = testFolder.resolve("contended.json");
        JsonAddressBookStorage first = new JsonAddressBookStorage(dataFile);
        JsonAddressBookStorage second = new JsonAddressBookStorage(dataFile);
        first.readAddressBook();
        second.readAddressBook();
        CountDownLatch serializationStarted = new CountDownLatch(1);
        CountDownLatch allowSerialization = new CountDownLatch(1);
        ReadOnlyAddressBook blocked = () -> {
            serializationStarted.countDown();
            try {
                if (!allowSerialization.await(5, TimeUnit.SECONDS)) {
                    throw new UncheckedIOException(new IOException("Timed out waiting to release writer."));
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new UncheckedIOException(new IOException(error));
            }
            return getTypicalAddressBook().getPersonList();
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> firstSave = executor.submit(() -> {
                try {
                    first.saveAddressBook(blocked);
                } catch (IOException error) {
                    throw new UncheckedIOException(error);
                }
            });
            assertTrue(serializationStarted.await(5, TimeUnit.SECONDS));
            assertThrows(IOException.class, () -> second.saveAddressBook(getTypicalAddressBook()));
            allowSerialization.countDown();
            firstSave.get(5, TimeUnit.SECONDS);
        } finally {
            allowSerialization.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    public void saveAddressBook_anotherJvmCannotAcquireLock() throws Exception {
        Path dataFile = testFolder.resolve("cross-process.json");
        Path lockPath = testFolder.resolve("cross-process.json.lock");
        Path readyPath = testFolder.resolve("lock-ready");
        Path releasePath = testFolder.resolve("release-lock");
        Path outputPath = testFolder.resolve("lock-process-output.txt");
        Path testClasses = Path.of(JsonAddressBookStorageTest.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        Process holder = new ProcessBuilder(java.toString(), "-cp", testClasses.toString(),
                StorageLockProcess.class.getName(), lockPath.toString(), readyPath.toString(), releasePath.toString())
                .redirectErrorStream(true).redirectOutput(outputPath.toFile()).start();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!Files.exists(readyPath) && holder.isAlive() && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
            String processOutput = Files.exists(outputPath) ? Files.readString(outputPath) : "no process output";
            assertTrue(Files.exists(readyPath), "Lock process failed: " + processOutput);

            JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
            assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));
        } finally {
            Files.createFile(releasePath);
            if (!holder.waitFor(5, TimeUnit.SECONDS)) {
                holder.destroyForcibly();
            }
        }
    }

    @Test
    public void saveAndReloadAddressBook_oneThousandClients_success() throws Exception {
        Path dataFile = testFolder.resolve("one-thousand-clients.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        storage.readAddressBook();
        AddressBook addressBook = new AddressBook();
        for (int index = 0; index < 1000; index++) {
            addressBook.addPerson(new PersonBuilder()
                    .withName("Client " + index)
                    .withPhone(String.format("%08d", index))
                    .withEmail("client" + index + "@example.com")
                    .build());
        }

        storage.saveAddressBook(addressBook);

        assertEquals(addressBook, new AddressBook(storage.readAddressBook().get()));
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
