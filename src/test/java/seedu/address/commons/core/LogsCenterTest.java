package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
