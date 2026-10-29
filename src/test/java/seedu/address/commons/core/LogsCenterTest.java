package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {

    @TempDir
    Path temporaryFolder;

    @Test
    public void addFileHandler_externalRotationLinks_preservesExternalFile() throws Exception {
        Path outside = temporaryFolder.resolve("outside.log");
        Files.writeString(outside, "Keep this external log");
        for (String suffix : List.of(".0", ".4", ".0.1", ".0.lck")) {
            Path home = Files.createDirectory(temporaryFolder.resolve("home" + suffix));
            try {
                Files.createSymbolicLink(home.resolve("addressbook.log" + suffix), outside);
            } catch (UnsupportedOperationException | FileSystemException exception) {
                assumeTrue(false, "Symbolic links are unavailable: " + exception);
            }
            Logger targetLogger = Logger.getAnonymousLogger();
            targetLogger.setUseParentHandlers(false);
            try {
                LogsCenter.addFileHandler(targetLogger, home.resolve("addressbook.log"));
                assertEquals(0, targetLogger.getHandlers().length);
                assertEquals("Keep this external log", Files.readString(outside));
            } finally {
                for (Handler handler : targetLogger.getHandlers()) {
                    handler.close();
                    targetLogger.removeHandler(handler);
                }
            }
        }
    }

    @Test
    public void addFileHandler_existingLog_preservesAndAppendsEntries() throws Exception {
        Path existingLog = temporaryFolder.resolve("addressbook.log.0");
        String previousEntry = "Existing log entry\n";
        Files.writeString(existingLog, previousEntry);
        Logger targetLogger = Logger.getAnonymousLogger();
        targetLogger.setUseParentHandlers(false);
        targetLogger.setLevel(Level.INFO);
        try {
            LogsCenter.addFileHandler(targetLogger, temporaryFolder.resolve("addressbook.log"));
            assertEquals(1, targetLogger.getHandlers().length);
            targetLogger.info("Appended after restart");
            targetLogger.getHandlers()[0].flush();

            String contents = Files.readString(existingLog);
            assertTrue(contents.startsWith(previousEntry));
            assertTrue(contents.contains("Appended after restart"));
        } finally {
            for (Handler handler : targetLogger.getHandlers()) {
                handler.close();
                targetLogger.removeHandler(handler);
            }
        }
    }

    @Test
    public void addFileHandler_folderWithPatternCharacters_writesInsideLiteralFolder() throws Exception {
        Path logFolder = Files.createDirectory(temporaryFolder.resolve("ClientDesk %t %h"));
        Logger targetLogger = Logger.getAnonymousLogger();
        targetLogger.setUseParentHandlers(false);
        LogsCenter.addFileHandler(targetLogger, logFolder.resolve("addressbook.log"));

        assertEquals(1, targetLogger.getHandlers().length);
        assertTrue(targetLogger.getHandlers()[0] instanceof FileHandler);
        Handler handler = targetLogger.getHandlers()[0];
        try {
            targetLogger.info("Stored in the literal application folder");
            handler.flush();
            assertTrue(Files.readString(logFolder.resolve("addressbook.log.0"))
                    .contains("Stored in the literal application folder"));
        } finally {
            handler.close();
            targetLogger.removeHandler(handler);
        }
    }

    @Test
    public void addFileHandler_unavailableLogPath_keepsExistingHandlerAvailable() throws Exception {
        Path blockingFile = Files.createFile(temporaryFolder.resolve("blocking-file"));
        Path logFile = blockingFile.resolve("addressbook.log");
        Logger targetLogger = Logger.getAnonymousLogger();
        targetLogger.setUseParentHandlers(false);
        targetLogger.setLevel(Level.ALL);
        List<LogRecord> records = new ArrayList<>();
        Handler recordingHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        targetLogger.addHandler(recordingHandler);

        LogsCenter.addFileHandler(targetLogger, logFile);
        targetLogger.info("Application still runs");

        assertEquals(2, records.size());
        assertEquals(Level.WARNING, records.get(0).getLevel());
        assertTrue(records.get(0).getThrown() instanceof IOException);
        assertEquals("Application still runs", records.get(1).getMessage());
        assertEquals(1, targetLogger.getHandlers().length);
    }
}
