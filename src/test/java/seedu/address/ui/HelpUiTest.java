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
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.JavaFxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class HelpUiTest {

    @TempDir
    public Path testFolder;

    @Test
    public void enterHelp_showsInstructionsAndOverviewWithoutSavingData() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Acme Studio").build());
        model.addPerson(new PersonBuilder().withName("Northstar Labs").withPhone("91234567")
                .withEmail("alex@northstar.io").build());
        model.updateFilteredPersonList(client -> client.getName().fullName.equals("Northstar Labs"));
        AddressBook originalData = new AddressBook(model.getAddressBook());
        List<?> originalDisplay = List.copyOf(model.getFilteredPersonList());
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

                input.setText("help edit-client");
                input.fireEvent(new ActionEvent());
                assertTrue(instructions.getText().contains("Format: edit-client CLIENT_INDEX e/EMAIL"));

                String previous = instructions.getText();
                for (String name : new String[] {"unknown-command", "undo", "add-note", "follow-up", "agenda"}) {
                    input.setText("help " + name);
                    input.fireEvent(new ActionEvent());
                    assertEquals(previous, instructions.getText());
                    assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                }

                MenuBar menu = (MenuBar) window.getPrimaryStage().getScene().lookup("#menuBar");
                menu.getMenus().get(1).getItems().getFirst().fire();
                assertTrue(instructions.getText().contains("ClientDesk commands:"));
                assertTrue(instructions.getText().contains("remark - "));
                for (String name : new String[] {"undo", "add-note", "follow-up", "agenda"}) {
                    assertFalse(instructions.getText().contains(name + " - "));
                }
                String overview = instructions.getText();
                input.setText("help remark");
                input.fireEvent(new ActionEvent());
                assertTrue(instructions.getText().contains("Format: remark INDEX r/[REMARK]"));
                input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                        false, false, false, false));
                assertEquals(overview, instructions.getText());
                assertTrue(help.isShowing());
                assertEquals(1, Window.getWindows().stream().filter(item -> item instanceof Stage stage
                        && stage.getTitle().equals("ClientDesk Help")).count());
                assertEquals(0, saveAttempts.get());
                assertEquals(originalData, model.getAddressBook());
                assertEquals(originalDisplay, model.getFilteredPersonList());
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }
}
