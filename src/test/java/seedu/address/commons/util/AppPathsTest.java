package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppPathsTest {

    @TempDir
    Path temporaryFolder;

    @Test
    public void resolveWithinHome_missingDescendants_returnsNormalizedPath() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path path = home.resolve("unused/../data/nested/addressbook.json");

        assertEquals(home.resolve("data/nested/addressbook.json").toAbsolutePath(),
                AppPaths.resolveWithinHome(path, home.toRealPath()));
    }

    @Test
    public void resolveWithinHome_siblingWithSamePrefix_rejects() throws Exception {
        Path home = Files.createDirectory(temporaryFolder.resolve("home"));
        Path sibling = Files.createDirectory(temporaryFolder.resolve("home-other"));

        Path realHome = home.toRealPath();
        assertThrows(IOException.class, () -> AppPaths.resolveWithinHome(sibling.resolve("data.json"), realHome));
    }

    @Test
    public void resolveHome_jarInFolderWithSpaces_usesJarFolderInsteadOfWorkingDirectory() throws Exception {
        Path jarFolder = Files.createDirectory(temporaryFolder.resolve("ClientDesk release files"));
        Path jarFile = Files.createFile(jarFolder.resolve("client desk.jar"));
        Path workingDirectory = Files.createDirectory(temporaryFolder.resolve("other working directory"));

        assertEquals(jarFolder, AppPaths.resolveHome(jarFile.toUri().toURL(), workingDirectory));
    }

    @Test
    public void resolveHome_classDirectory_usesWorkingDirectory() throws Exception {
        Path classDirectory = Files.createDirectory(temporaryFolder.resolve("classes"));
        Path workingDirectory = Files.createDirectory(temporaryFolder.resolve("workspace"));

        assertEquals(workingDirectory, AppPaths.resolveHome(classDirectory.toUri().toURL(), workingDirectory));
        assertEquals(workingDirectory, AppPaths.resolveHome(null, workingDirectory));
    }

    @Test
    public void getHome_classDirectoryLaunch_usesCurrentWorkingDirectory() {
        assertEquals(Path.of("").toAbsolutePath().normalize(), AppPaths.getHome());
    }

    @Test
    public void resolveHome_nonFileCodeSource_usesWorkingDirectory() throws Exception {
        Path workingDirectory = Files.createDirectory(temporaryFolder.resolve("workspace"));

        assertEquals(workingDirectory, AppPaths.resolveHome(
                URI.create("https://example.invalid/clientdesk.jar").toURL(), workingDirectory));
    }
}
