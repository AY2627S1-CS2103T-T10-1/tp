package seedu.address.commons.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
     * Returns a normalized path whose real target stays inside the pinned, canonical {@code realHome}.
     * Missing files are permitted only when their nearest existing ancestor remains inside the real home.
     * The caller captures {@code realHome} with {@link Path#toRealPath} before runtime file access begins.
     *
     * @throws IOException if the path escapes the home or an existing path cannot be resolved
     */
    public static Path resolveWithinHome(Path path, Path realHome) throws IOException {
        Path normalizedPath = path.toAbsolutePath().normalize();
        Path existing = normalizedPath;
        while (Files.notExists(existing, LinkOption.NOFOLLOW_LINKS)) {
            existing = existing.getParent();
        }
        if (!existing.toRealPath().startsWith(realHome)) {
            throw new IOException("Application file links must stay inside the ClientDesk home folder.");
        }
        return normalizedPath;
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
