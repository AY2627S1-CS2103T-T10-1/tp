package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppPathsTest {

    @TempDir
    Path temporaryFolder;

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
}
