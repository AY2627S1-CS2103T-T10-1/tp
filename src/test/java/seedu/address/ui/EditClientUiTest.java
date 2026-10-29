package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
import seedu.address.testutil.ClientWorkspaceTestUtil;
import seedu.address.testutil.JavaFxTestUtil;

public class EditClientUiTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_saveFailure_preservesEmailSearchSelectionAndDetails() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                throw new IOException("Simulated write failure");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                ClientWorkspaceTestUtil.selectDetailsTab(window);
                TextArea details = (TextArea) window.getPrimaryStage().getScene().lookup("#clientDetails");
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                enter(input, "find " + ALICE.getEmail().value);
                enter(input, "view 1");
                String previousDetails = details.getText();

                enter(input, "edit-client 1 e/replacement@example.com");

                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
                assertEquals(List.of(ALICE), model.getFilteredPersonList());
                assertEquals(ALICE, window.getPersonListPanel().selectedClientProperty().get());
                assertEquals(previousDetails, details.getText());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
