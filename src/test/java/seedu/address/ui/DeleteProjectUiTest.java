package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

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
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

/** Exercises project deletion through the application's real command box. */
public class DeleteProjectUiTest {
    @TempDir
    public Path testFolder;

    @Test
    public void enterDeleteProject_deletesSelectedFilteredClientProjectAndPersists() throws Exception {
        Project alphaProject = project("Alpha work");
        Project zuluProject = project("Zulu work");
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alpha").withEmail("alpha@example.com")
                .withProjects(List.of(alphaProject), 2).build());
        model.addPerson(new PersonBuilder().withName("Zulu").withEmail("zulu@example.com")
                .withProjects(List.of(zuluProject), 2).build());
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

                input.setText("delete-project 1 1");
                input.fireEvent(new ActionEvent());

                Person updatedZulu = model.getFilteredPersonList().getFirst();
                assertEquals("Deleted project #1 from Zulu.", feedback.getText());
                assertEquals(1, clients.getItems().size());
                assertEquals(updatedZulu, clients.getItems().getFirst());
                assertTrue(updatedZulu.getProjects().isEmpty());
                assertEquals(2, updatedZulu.getNextProjectId());
                assertEquals(List.of(alphaProject), model.getAddressBook().getPersonList().getFirst().getProjects());
                assertTrue(input.getText().isEmpty());

                input.setText("delete-project 1 1");
                input.fireEvent(new ActionEvent());
                assertEquals("No project with ID 1 exists for this client.", feedback.getText());
                assertTrue(input.getStyleClass().contains("error"));
                assertTrue(model.getFilteredPersonList().getFirst().getProjects().isEmpty());
            } finally {
                window.getPrimaryStage().hide();
            }
        });
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private static Project project(String title) {
        return new Project(1, new ProjectTitle(title), new ProjectDeadline("9999-12-31"),
                ProjectStatus.NOT_STARTED, ProjectPriority.MEDIUM);
    }
}
