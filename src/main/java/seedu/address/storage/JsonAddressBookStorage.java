package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);
    private final Map<Path, FileSnapshot> observedFiles = new ConcurrentHashMap<>();

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Path normalizedPath = normalize(filePath);
        if (Files.notExists(normalizedPath, LinkOption.NOFOLLOW_LINKS)) {
            observedFiles.put(normalizedPath, FileSnapshot.missing());
            return Optional.empty();
        }

        byte[] contents;
        JsonSerializableAddressBook jsonAddressBook;
        try {
            contents = Files.readAllBytes(normalizedPath);
            String json = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(contents)).toString();
            jsonAddressBook = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
            if (jsonAddressBook == null) {
                throw new IOException("JSON root must not be null.");
            }
        } catch (IOException error) {
            logger.warning("Error reading from jsonFile file " + normalizedPath + ": " + error);
            throw new DataLoadingException(error);
        }
        try {
            ReadOnlyAddressBook addressBook = jsonAddressBook.toModelType();
            observedFiles.put(normalizedPath, FileSnapshot.present(contents));
            return Optional.of(addressBook);
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path destination = normalize(filePath);
        Files.createDirectories(destination.getParent());
        Path lockPath = destination.resolveSibling(destination.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            FileLock lock;
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException error) {
                throw new IOException("Another ClientDesk instance is saving this data file.", error);
            }
            if (lock == null) {
                throw new IOException("Another ClientDesk instance is saving this data file.");
            }
            try (FileLock heldLock = lock) {
                verifyUnchangedSinceRead(destination);
                byte[] savedContents = saveUnderLock(addressBook, destination);
                observedFiles.put(destination, FileSnapshot.present(savedContents));
            }
        }
    }

    private void verifyUnchangedSinceRead(Path destination) throws IOException {
        FileSnapshot expected = observedFiles.get(destination);
        if (expected == null) {
            return; // Preserve the explicit first-save API for storage clients that have not read first.
        }
        FileSnapshot actual;
        try {
            actual = FileSnapshot.present(Files.readAllBytes(destination));
        } catch (NoSuchFileException error) {
            actual = FileSnapshot.missing();
        }
        if (!expected.equals(actual)) {
            throw new IOException("Data changed since load. Close and reopen ClientDesk to reload.");
        }
    }

    private static byte[] saveUnderLock(ReadOnlyAddressBook addressBook, Path destination) throws IOException {
        Path temporary = Files.createTempFile(destination.getParent(), "clientdesk-", ".tmp");
        try {
            JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), temporary);
            byte[] savedContents = Files.readAllBytes(temporary);
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return savedContents;
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException error) {
                logger.warning("Could not remove temporary data file: " + temporary);
            }
        }
    }

    private static Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private static final class FileSnapshot {
        private final boolean exists;
        private final byte[] contents;

        private FileSnapshot(boolean exists, byte[] contents) {
            this.exists = exists;
            this.contents = contents;
        }

        static FileSnapshot missing() {
            return new FileSnapshot(false, new byte[0]);
        }

        static FileSnapshot present(byte[] contents) {
            return new FileSnapshot(true, contents);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FileSnapshot)) {
                return false;
            }
            FileSnapshot that = (FileSnapshot) other;
            return exists == that.exists && Arrays.equals(contents, that.contents);
        }

        @Override
        public int hashCode() {
            return 31 * Boolean.hashCode(exists) + Arrays.hashCode(contents);
        }
    }

}
