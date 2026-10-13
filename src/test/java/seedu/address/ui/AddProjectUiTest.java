package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class AddProjectUiTest {

    @TempDir
    public Path testFolder;

    @Test
    public void enterAddProject_savesSelectsAndDisplaysProjectWithoutClearingFilter() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Zulu").withEmail("zulu@example.com").build());
        model.addPerson(new PersonBuilder().withName("Alpha").withEmail("alpha@example.com").build());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Zulu"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                ListView<?> clients = (ListView<?>) window.getPrimaryStage().getScene().lookup("#personListView");
                TextArea feedback = (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
                input.setText("add-project 1 title/Website deadline/9999-12-31 priority/high");
                input.fireEvent(new ActionEvent());

                Person selected = (Person) clients.getSelectionModel().getSelectedItem();
                assertEquals("Zulu", selected.getName().fullName);
                assertEquals(1, selected.getProjects().size());
                assertEquals(1, clients.getItems().size());
                assertEquals("Added project #1 to Zulu.", feedback.getText());
                assertTrue(input.getText().isEmpty());
                PersonCard card = new PersonCard(selected, 1);
                javafx.scene.control.Label count = (javafx.scene.control.Label) card.getRoot().lookup("#projects");
                assertEquals("Projects: 1 (use view 1 for details)", count.getText());

                input.setText("view 1");
                input.fireEvent(new ActionEvent());
                assertTrue(feedback.getText().contains("#1 | Website | Deadline: 9999-12-31"));
                input.setText("add-project 1 title/website deadline/9999-12-31");
                input.fireEvent(new ActionEvent());
                assertEquals("This project already exists for Zulu.", feedback.getText());
                assertEquals(selected, clients.getSelectionModel().getSelectedItem());
                assertEquals(1, model.getFilteredPersonList().getFirst().getProjects().size());
                assertTrue(input.getStyleClass().contains("error"));
            } finally {
                window.getPrimaryStage().hide();
            }
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
