package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;

public class AddClientUiTest {

    @TempDir
    public Path testFolder;

    @Test
    public void enterAddClient_savesDisplaysAndSelectsNewClient() throws Exception {
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
                input.setText("add-client n/O'Neil & Co. (SG) p/+65 8123-4567 e/hello@acme.sg t/web-design");
                input.fireEvent(new ActionEvent());

                ListView<?> list = (ListView<?>) window.getPrimaryStage().getScene().lookup("#personListView");
                Person selected = (Person) list.getSelectionModel().getSelectedItem();
                assertNotNull(selected);
                assertEquals("O'Neil & Co. (SG)", selected.getName().fullName);
                assertEquals("6581234567", selected.getPhone().value);
                assertEquals(model.getFilteredPersonList().get(0), selected);
                assertTrue(input.getText().isEmpty());

                input.setText("add-client n/Other p/99998888 e/HELLO@ACME.SG");
                input.fireEvent(new ActionEvent());
                assertEquals(1, list.getItems().size());
                assertEquals(selected, list.getSelectionModel().getSelectedItem());
                assertTrue(input.getStyleClass().contains("error"));
            } finally {
                window.getPrimaryStage().hide();
            }
        });

        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());
    }
}
