package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;

/** Exercises command logging and user feedback through the real command box. */
public class CommandLoggingUiTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_allLogLevels_omitsClientDataAndRetainsUserFeedback() throws Exception {
        Logger baseLogger = LogsCenter.getLogger(CommandLoggingUiTest.class).getParent();
        Level previousLevel = baseLogger.getLevel();
        ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
        StreamHandler capture = new StreamHandler(logOutput, new SimpleFormatter());
        capture.setLevel(Level.ALL);
        baseLogger.addHandler(capture);
        baseLogger.setLevel(Level.ALL);
        try {
            ModelManager model = new ModelManager();
            JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json"));
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
            JavaFxTestUtil.runOnFxThread(() -> {
                MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
                try {
                    window.fillInnerParts();
                    window.show();
                    TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                    TextArea feedback = (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
                    enter(input, "add-client n/PrivateNameCanary p/91234567 e/privacy-canary@example.com"
                            + " t/private-tag-canary");
                    assertTrue(feedback.getText().contains("PrivateNameCanary"));
                    enter(input, "remark 1 r/ConfidentialRemarkCanary");
                    assertTrue(feedback.getText().contains("Added remark to person: PrivateNameCanary"));
                    enter(input, "view 1");
                    assertTrue(feedback.getText().contains("privacy-canary@example.com"));
                    assertTrue(feedback.getText().contains("ConfidentialRemarkCanary"));
                    enter(input, "edit-client 1 e/rejected-canary-value");
                    assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                    enter(input, "UnrecognizedCanary");
                    assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                } finally {
                    List.copyOf(Window.getWindows()).forEach(Window::hide);
                }
            });
        } finally {
            baseLogger.removeHandler(capture);
            baseLogger.setLevel(previousLevel);
            capture.close();
        }
        String logs = logOutput.toString(StandardCharsets.UTF_8);
        for (String canary : List.of("PrivateNameCanary", "91234567", "privacy-canary@example.com",
                "private-tag-canary", "ConfidentialRemarkCanary", "rejected-canary-value", "UnrecognizedCanary")) {
            assertFalse(logs.toLowerCase(Locale.ROOT).contains(canary.toLowerCase(Locale.ROOT)),
                    "Log contains private canary: " + canary);
        }
        assertTrue(logs.contains("AddClientCommand"));
        assertTrue(logs.contains("ViewCommand"));
        assertTrue(logs.contains("RemarkCommand"));
        assertTrue(logs.contains("Command completed"));
        assertTrue(logs.contains("Command rejected"));
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
