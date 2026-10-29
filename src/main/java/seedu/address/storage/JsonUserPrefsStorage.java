package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.AppPaths;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file
 */
public class JsonUserPrefsStorage {

    private Path filePath;
    private final Path allowedHome;

    /**
     * Creates preference storage for a caller-managed path without a runtime home boundary.
     */
    public JsonUserPrefsStorage(Path filePath) {
        this.filePath = filePath;
        this.allowedHome = null;
    }

    /**
     * Creates preference storage limited to the current real location of {@code allowedHome}.
     *
     * @throws IOException if the home folder cannot be resolved
     */
    public JsonUserPrefsStorage(Path filePath, Path allowedHome) throws IOException {
        this.filePath = filePath;
        this.allowedHome = allowedHome.toRealPath();
    }

    public Path getUserPrefsFilePath() {
        return filePath;
    }

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return readUserPrefs(filePath);
    }

    /**
     * Similar to {@link #readUserPrefs()}
     * @param prefsFilePath location of the data. Cannot be null.
     * @throws DataLoadingException if the file format is not as expected.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        try {
            return JsonUtil.readJsonFile(resolveFilePath(prefsFilePath), UserPrefs.class);
        } catch (IOException exception) {
            throw new DataLoadingException(exception);
        }
    }

    /**
     * Saves the given {@link seedu.address.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        JsonUtil.saveJsonFile(userPrefs, resolveFilePath(filePath));
    }

    private Path resolveFilePath(Path path) throws IOException {
        return allowedHome == null ? path : AppPaths.resolveWithinHome(path, allowedHome);
    }

}
