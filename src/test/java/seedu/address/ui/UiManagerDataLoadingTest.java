package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;

/**
 * Tests the recovery instructions shown when client data cannot be loaded.
 */
public class UiManagerDataLoadingTest {

    @TempDir
    Path temporaryFolder;

    @Test
    public void dataLoadingErrorMessage_identifiesFileAndRecoverySteps() {
        Path dataFile = Path.of("data", "addressbook.json");
        String message = UiManager.dataLoadingErrorMessage(dataFile);

        assertTrue(message.contains(dataFile.toAbsolutePath().toString()));
        assertTrue(message.contains("has not changed this file"));
        assertTrue(message.contains("Back it up"));
        assertTrue(message.contains("restart ClientDesk"));
    }

    @Test
    public void createDataLoadingErrorAlert_beforeMainWindowExists_showsWithoutOwner() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Alert alert = UiManager.createDataLoadingErrorAlert(Path.of("data", "addressbook.json"));
            try {
                assertNull(alert.getOwner());
                alert.show();
                assertTrue(alert.isShowing());
            } finally {
                alert.close();
            }
        });
    }

    @Test
    public void showAlertDialogAndWait_afterMainWindowStarts_preservesOwnerAndMessage() throws Exception {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("clients.json"));
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        UiManager ui = new UiManager(new LogicManager(new ModelManager(), storage),
                addressBookStorage.getAddressBookFilePath());

        JavaFxTestUtil.runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            AtomicBoolean alertObserved = new AtomicBoolean();
            AtomicReference<Throwable> observationFailure = new AtomicReference<>();
            try {
                ui.start(primaryStage);
                assertTrue(primaryStage.isShowing());

                Platform.runLater(() -> {
                    try {
                        Stage alertStage = (Stage) Window.getWindows().stream()
                                .filter(window -> window instanceof Stage stage && stage.isShowing()
                                        && stage.getTitle().equals("Owned alert"))
                                .findFirst().orElseThrow();
                        alertObserved.set(true);
                        DialogPane pane = (DialogPane) alertStage.getScene()
                                .lookup("#" + UiManager.ALERT_DIALOG_PANE_FIELD_ID);
                        assertNotNull(pane);
                        assertSame(primaryStage, alertStage.getOwner());
                        assertEquals("Owned alert", alertStage.getTitle());
                        assertEquals("Existing warning", pane.getHeaderText());
                        assertEquals("An error was handled.", pane.getContentText());
                    } catch (Throwable error) {
                        observationFailure.set(error);
                    } finally {
                        List.copyOf(Window.getWindows()).stream()
                                .filter(window -> window != primaryStage).forEach(Window::hide);
                    }
                });

                ui.showAlertDialogAndWait(AlertType.ERROR, "Owned alert", "Existing warning",
                        "An error was handled.");
                if (observationFailure.get() != null) {
                    throw new AssertionError("The owned alert did not retain its owner and message.",
                            observationFailure.get());
                }
                assertTrue(alertObserved.get());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }
}
