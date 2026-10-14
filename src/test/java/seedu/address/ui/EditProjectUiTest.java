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
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises project editing, selection and validation through the real command box.
 */
public class EditProjectUiTest {
    @TempDir
    public Path testFolder;

    @Test
    public void enterEditProject_updatesSelectedFilteredClientAndPersists() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Zulu").withEmail("zulu@example.com").build());
        model.addPerson(new PersonBuilder().withName("Alpha").withEmail("alpha@example.com").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        logic.execute("find zulu");

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                ListView<?> clients = (ListView<?>) window.getPrimaryStage().getScene().lookup("#personListView");
                TextArea feedback = (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
                input.setText("add-project 1 title/Website deadline/9999-12-31");
                input.fireEvent(new ActionEvent());
                input.setText("edit-project 1 1 status/completed priority/high");
                input.fireEvent(new ActionEvent());

                Person edited = model.getFilteredPersonList().getFirst();
                assertEquals("Updated project #1 for Zulu: Website | Deadline: 9999-12-31"
                        + " | Status: completed | Priority: high", feedback.getText());
                assertEquals(edited, clients.getSelectionModel().getSelectedItem());
                assertEquals(1, clients.getItems().size());
                assertEquals(2, edited.getNextProjectId());
                assertEquals(ProjectStatus.COMPLETED, edited.getProjects().getFirst().getStatus());
                assertEquals(ProjectPriority.HIGH, edited.getProjects().getFirst().getPriority());
                assertTrue(input.getText().isEmpty());

                input.setText("view 1");
                input.fireEvent(new ActionEvent());
                assertTrue(feedback.getText().contains("#1 | Website | Deadline: 9999-12-31"
                        + " | Status: completed | Priority: high"));
                input.setText("edit-project 1 1 priority/urgent");
                input.fireEvent(new ActionEvent());
                assertEquals(ProjectPriority.MESSAGE_CONSTRAINTS, feedback.getText());
                assertTrue(input.getStyleClass().contains("error"));
                assertEquals(edited, model.getFilteredPersonList().getFirst());
            } finally {
                window.getPrimaryStage().hide();
            }
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertTrue(model.getAddressBook().getPersonList().get(1).getProjects().isEmpty());
    }
}
