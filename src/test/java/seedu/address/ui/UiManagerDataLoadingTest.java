package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Alert;
import seedu.address.testutil.JavaFxTestUtil;

/**
 * Tests the recovery instructions shown when client data cannot be loaded.
 */
public class UiManagerDataLoadingTest {

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
}
