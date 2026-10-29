package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class LegacyClientSelectionUiTest {

    @TempDir
    public Path testFolder;

    @Test
    public void legacyAddAndEdit_selectSavedClientAfterListRefresh() throws Exception {
        ModelManager model = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").withPhone("81111111")
                .withEmail("alice@example.com").build();
        Person benson = new PersonBuilder().withName("Benson").withPhone("82222222")
                .withEmail("benson@example.com").build();
        Person carol = new PersonBuilder().withName("Carol").withPhone("83333333")
                .withEmail("carol@example.com").build();
        model.addPerson(alice);
        model.addPerson(benson);
        model.addPerson(carol);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                TextArea details = (TextArea) window.getPrimaryStage().getScene().lookup("#clientDetails");
                ListView<?> clients = (ListView<?>) window.getPrimaryStage().getScene().lookup("#personListView");

                window.getPersonListPanel().selectClient(carol);
                enter(input, "add n/Aaron p/84444444 e/aaron@example.com a/Home");
                Person added = (Person) clients.getSelectionModel().getSelectedItem();
                assertEquals("Aaron", added.getName().fullName);
                assertEquals(0, clients.getSelectionModel().getSelectedIndex());
                assertTrue(details.getText().contains("Name: Aaron"));
                assertEquals(List.of(added, alice, benson, carol), model.getFilteredPersonList());

                enter(input, "find Benson");
                enter(input, "view 1");
                enter(input, "edit 1 n/Zulu");
                Person edited = (Person) clients.getSelectionModel().getSelectedItem();
                assertEquals("Zulu", edited.getName().fullName);
                assertEquals(model.getFilteredPersonList().size() - 1, clients.getSelectionModel().getSelectedIndex());
                assertTrue(details.getText().contains("Name: Zulu"));
                assertEquals(List.of(added, alice, carol, edited), model.getFilteredPersonList());
                assertTrue(input.getText().isEmpty());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void failedLegacyAddAndEdit_preservePreviousSelectionAndDetails() throws Exception {
        ModelManager model = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").withPhone("81111111")
                .withEmail("alice@example.com").build();
        Person benson = new PersonBuilder().withName("Benson").withPhone("82222222")
                .withEmail("benson@example.com").build();
        model.addPerson(alice);
        model.addPerson(benson);
        boolean[] failSave = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("failed-clients.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                if (failSave[0]) {
                    throw new IOException("Simulated write failure");
                }
                super.saveAddressBook(data);
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("failed-preferences.json"))));

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                TextArea details = (TextArea) window.getPrimaryStage().getScene().lookup("#clientDetails");
                ListView<?> clients = (ListView<?>) window.getPrimaryStage().getScene().lookup("#personListView");
                window.getPersonListPanel().selectClient(benson);
                enter(input, "view 2");
                String previousDetails = details.getText();
                failSave[0] = true;

                enter(input, "add n/Owen p/85555555 e/owen@example.com a/Home");
                assertEquals(benson, clients.getSelectionModel().getSelectedItem());
                assertEquals(previousDetails, details.getText());
                assertEquals(List.of(alice, benson), model.getAddressBook().getPersonList());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                enter(input, "edit 2 n/Zulu");
                assertEquals(benson, clients.getSelectionModel().getSelectedItem());
                assertEquals(previousDetails, details.getText());
                assertEquals(List.of(alice, benson), model.getAddressBook().getPersonList());
                assertFalse(input.getText().isEmpty());
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
