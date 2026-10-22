package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
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

/**
 * Exercises the details panel through the real command box and list selection.
 */
public class ClientDetailsUiTest {

    @TempDir
    public Path testFolder;

    @Test
    public void viewAndSelect_showDetailsAndPreserveThemOnErrorsWithoutSaving() throws Exception {
        ModelManager model = new ModelManager();
        Project later = new Project(11, new ProjectTitle("Website launch"), new ProjectDeadline("2026-10-30"),
                ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH);
        Project earlier = new Project(2, new ProjectTitle("Initial draft"), new ProjectDeadline("2026-10-20"),
                ProjectStatus.NOT_STARTED, ProjectPriority.LOW);
        Person client = ALICE.withProjects(List.of(later, earlier), 12);
        model.addPerson(client);
        model.addPerson(BENSON);
        AtomicInteger saveAttempts = new AtomicInteger();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                saveAttempts.incrementAndGet();
                throw new IOException("Viewing must work without write access");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                assertEquals("ClientDesk", window.getPrimaryStage().getTitle());
                TextArea details = (TextArea) window.getPrimaryStage().getScene().lookup("#clientDetails");
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                assertEquals(ClientDetailsPanel.MESSAGE_EMPTY, details.getText());
                assertFalse(details.isEditable());
                assertTrue(details.isWrapText());
                enter(input, "view 1");
                assertTrue(input.getText().isEmpty());
                assertTrue(details.getText().contains("Name: Alice Pauline"));
                assertTrue(details.getText().contains("Phone: " + ALICE.getPhone()));
                assertTrue(details.getText().contains("Email: " + ALICE.getEmail()));
                assertTrue(details.getText().contains("#11 | Website launch | Deadline: 2026-10-30"
                        + " | Status: in-progress | Priority: high"));
                assertTrue(details.getText().indexOf("#2 |") < details.getText().indexOf("#11 |"));
                String previous = details.getText();
                for (String invalid : List.of("view 999", "view abc")) {
                    enter(input, invalid);
                    assertEquals(previous, details.getText());
                    assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                }
                enter(input, "find Benson");
                enter(input, "view 1");
                assertTrue(details.getText().contains("Name: Benson Meier"));
                assertTrue(details.getText().contains("Projects: No projects yet."));
                assertFalse(details.getText().contains("Website launch"));
                assertFalse(details.getText().contains("Initial draft"));
                assertEquals(0, details.getCaretPosition());
                enter(input, "list-clients");
                window.getPersonListPanel().selectClient(client);
                assertTrue(details.getText().contains("Name: Alice Pauline"));
                ListView<?> clients = (ListView<?>) window.getPrimaryStage().getScene().lookup("#personListView");
                clients.getSelectionModel().clearSelection();
                assertEquals(ClientDetailsPanel.MESSAGE_EMPTY, details.getText());
                assertEquals(0, saveAttempts.get());
                assertEquals(List.of(client, BENSON), model.getAddressBook().getPersonList());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void projectChanges_refreshSelectedClientAndClearDeletedClient() throws Exception {
        checkProjectRefresh(false);
    }

    @Test
    public void projectChanges_filteredList_refreshCorrectClientAndPreserveDetailsOnFailure() throws Exception {
        checkProjectRefresh(true);
    }

    /**
     * Exercises successful and failed project mutations through the command box with disposable storage.
     */
    private void checkProjectRefresh(boolean filtered) throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AtomicBoolean failSave = new AtomicBoolean();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("refresh-clients.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                if (failSave.get()) {
                    throw new IOException("Simulated write failure");
                }
                super.saveAddressBook(data);
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("refresh-preferences.json"))));
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                TextArea details = (TextArea) window.getPrimaryStage().getScene().lookup("#clientDetails");
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                if (filtered) {
                    enter(input, "find Benson");
                }
                int index = filtered ? 1 : 2;
                enter(input, "view " + index);
                enter(input, "add-project " + index + " title/Website deadline/2026-12-01");
                assertTrue(input.getText().isEmpty());
                assertTrue(details.getText().contains("Name: Benson Meier"));
                assertTrue(details.getText().contains("#1 | Website"));
                assertEquals(model.getFilteredPersonList().get(index - 1),
                        window.getPersonListPanel().selectedClientProperty().get());
                assertTrue(model.getAddressBook().getPersonList().getFirst().getProjects().isEmpty());
                assertEquals(filtered ? 1 : 2, model.getFilteredPersonList().size());

                String previous = details.getText();
                for (String invalid : List.of("delete-project " + index + " 999",
                        "add-project " + index + " title/Invalid deadline/not-a-date")) {
                    enter(input, invalid);
                    assertEquals(previous, details.getText());
                }
                failSave.set(true);
                for (String failed : List.of("delete-project " + index + " 1",
                        "add-project " + index + " title/Another deadline/2026-12-02")) {
                    enter(input, failed);
                    assertEquals(previous, details.getText());
                    assertEquals(model.getFilteredPersonList().get(index - 1),
                            window.getPersonListPanel().selectedClientProperty().get());
                }
                failSave.set(false);
                enter(input, "add-project " + index + " title/Second deadline/2026-12-02");
                enter(input, "delete-project " + index + " 1");
                assertFalse(details.getText().contains("#1 | Website"));
                assertTrue(details.getText().contains("#2 | Second"));
                enter(input, "delete-project " + index + " 2");
                assertTrue(details.getText().contains("Projects: No projects yet."));
                assertTrue(details.getText().contains("Name: Benson Meier"));
                enter(input, "delete-client " + index);
                assertTrue(input.getText().isEmpty());
                assertEquals(ClientDetailsPanel.MESSAGE_EMPTY, details.getText());
                assertEquals(null, window.getPersonListPanel().selectedClientProperty().get());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    /** Enters a command as if the user pressed Enter in the command box. */
    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
