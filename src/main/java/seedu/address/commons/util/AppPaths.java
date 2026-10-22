package seedu.address.commons.util;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

/**
 * Resolves the folder where ClientDesk stores its local files.
 */
public final class AppPaths {

    private AppPaths() {
    }

    /**
     * Returns the JAR's folder for a packaged app, or the working directory for a class-directory launch.
     */
    public static Path getHome() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        CodeSource codeSource = AppPaths.class.getProtectionDomain().getCodeSource();
        return resolveHome(codeSource == null ? null : codeSource.getLocation(), workingDirectory);
    }

    /**
     * Returns the containing folder of a code archive, or the working directory for classes.
     */
    static Path resolveHome(URL codeLocation, Path workingDirectory) {
        Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        if (codeLocation == null) {
            return normalizedWorkingDirectory;
        }
        try {
            Path codeSource = Path.of(codeLocation.toURI());
            if (Files.isRegularFile(codeSource)) {
                return codeSource.toAbsolutePath().normalize().getParent();
            }
        } catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException exception) {
            return normalizedWorkingDirectory;
        }
        return normalizedWorkingDirectory;
    }
}
