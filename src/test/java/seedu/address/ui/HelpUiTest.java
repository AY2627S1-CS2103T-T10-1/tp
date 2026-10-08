package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;

public class HelpUiTest {

    @TempDir
    public Path testFolder;

    @Test
    public void enterHelp_showsInstructionsAndOverviewWithoutSavingData() throws Exception {
        ModelManager model = new ModelManager();
        AtomicInteger saveAttempts = new AtomicInteger();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                saveAttempts.incrementAndGet();
                throw new IOException("The data directory is read-only");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                input.setText("help ADD-CLIENT");
                input.fireEvent(new ActionEvent());
                Stage help = (Stage) Window.getWindows().stream()
                        .filter(item -> item instanceof Stage stage && stage.getTitle().equals("ClientDesk Help"))
                        .findFirst().orElseThrow();
                TextArea instructions = (TextArea) help.getScene().lookup("#commandHelp");
                assertTrue(instructions.getText().contains("Format: add-client n/NAME p/PHONE e/EMAIL [t/TAG]..."));
                assertTrue(instructions.getText().contains("7–15 digits"));
                assertFalse(instructions.isEditable());
                assertTrue(input.getText().isEmpty());

                String previous = instructions.getText();
                input.setText("help unknown-command");
                input.fireEvent(new ActionEvent());
                assertEquals(previous, instructions.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                // The Help menu/F1 action reuses and focuses the existing window with the overview.
                window.handleHelp();
                assertTrue(instructions.getText().contains("ClientDesk commands:"));
                assertTrue(instructions.getText().contains("agenda - "));
                assertTrue(help.isShowing());
                assertEquals(0, saveAttempts.get());
                assertTrue(model.getAddressBook().getPersonList().isEmpty());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }
}
