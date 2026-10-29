package seedu.address.storage;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Child process used to verify that save locks coordinate across JVMs. */
public final class StorageLockProcess {

    private StorageLockProcess() {
    }

    public static void main(String[] args) throws Exception {
        Path lockPath = Path.of(args[0]);
        Path readyPath = Path.of(args[1]);
        Path releasePath = Path.of(args[2]);
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock ignored = channel.lock()) {
            Files.createFile(readyPath);
            long deadline = System.nanoTime() + 10_000_000_000L;
            while (!Files.exists(releasePath) && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
        }
    }
}
