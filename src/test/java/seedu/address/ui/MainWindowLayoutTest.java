package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectDeadline;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.project.ProjectTitle;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ClientWorkspaceTestUtil;
import seedu.address.testutil.JavaFxTestUtil;

public class MainWindowLayoutTest {
    private static final Rectangle2D MINIMUM_SCREEN = new Rectangle2D(0, 0, 853, 480);

    @TempDir
    public Path testFolder;

    @Test
    public void resize_minimumScreen_keepsControlsWithinWindow() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            for (double height : List.of(480.0, 432.0, 400.0)) {
                MainWindow window = createWindow(new GuiSettings());
                Stage stage = window.getPrimaryStage();
                try {
                    window.fillInnerParts();
                    stage.setWidth(MINIMUM_SCREEN.getWidth());
                    stage.setHeight(height);
                    window.show();
                    Scene scene = stage.getScene();
                    TextField input = (TextField) scene.lookup("#commandTextField");
                    input.setText("view 1");
                    input.getOnAction().handle(new ActionEvent());
                    ClientWorkspaceTestUtil.selectDetailsTab(window);
                    scene.getRoot().applyCss();
                    scene.getRoot().layout();
                    scene.snapshot(null);

                    assertTrue(stage.getMinHeight() <= height);
                    assertTrue(stage.getWidth() <= MINIMUM_SCREEN.getWidth());
                    assertTrue(stage.getHeight() <= height);
                    for (String id : List.of("commandTextField", "resultDisplay", "personListView",
                            "clientDetails", "statusbarPlaceholder")) {
                        Node control = scene.lookup("#" + id);
                        Bounds bounds = control.localToScene(control.getBoundsInLocal());
                        assertTrue(bounds.getWidth() > 0 && bounds.getHeight() > 0, id);
                        assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= scene.getWidth(), id);
                        assertTrue(bounds.getMinY() >= 0 && bounds.getMaxY() <= scene.getHeight(), id);
                    }
                    TextArea details = (TextArea) scene.lookup("#clientDetails");
                    assertTrue(details.getText().contains("#12 | Website launch 12"));
                    details.setScrollTop(Double.MAX_VALUE);
                    assertTrue(details.getScrollTop() > 0);
                } finally {
                    stage.hide();
                }
            }
        });
    }

    @Test
    public void initialSize_smallScreen_fitsDefaultAndRestoredSettings() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = createWindow(new GuiSettings());
            Stage stage = window.getPrimaryStage();
            window.setWindowDefaultSize(new GuiSettings(), MINIMUM_SCREEN);
            assertEquals(740, stage.getWidth());
            assertEquals(480, stage.getHeight());

            window.setWindowDefaultSize(new GuiSettings(1400, 900, 800, 450), MINIMUM_SCREEN);
            assertGeometry(stage, 853, 480, 0, 0);

            window.setWindowDefaultSize(new GuiSettings(700, 440, 20, 30), MINIMUM_SCREEN);
            assertGeometry(stage, 700, 440, 20, 30);

            window.setWindowDefaultSize(new GuiSettings(100, 100, 800, 450), MINIMUM_SCREEN);
            assertGeometry(stage, 650, 400, 203, 80);

            Rectangle2D desktopArea = new Rectangle2D(0, 24, 853, 432);
            window.setWindowDefaultSize(new GuiSettings(900, 600, 100, 100), desktopArea);
            assertGeometry(stage, 853, 432, 0, 24);
        });
    }

    @Test
    public void restoredPosition_secondaryScreens_preservesValidAndClampsOverflowingCoordinates() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = createWindow(new GuiSettings());
            Stage stage = window.getPrimaryStage();
            Rectangle2D leftScreen = new Rectangle2D(-853, 40, 853, 480);
            window.setWindowDefaultSize(new GuiSettings(700, 440, -830, 60), leftScreen);
            assertGeometry(stage, 700, 440, -830, 60);

            window.setWindowDefaultSize(new GuiSettings(740, 450, -500, 100), leftScreen);
            assertGeometry(stage, 740, 450, -740, 70);

            Rectangle2D upperScreen = new Rectangle2D(1920, -480, 853, 480);
            window.setWindowDefaultSize(new GuiSettings(900, 600, 2000, -400), upperScreen);
            assertGeometry(stage, 853, 480, 1920, -480);
        });
    }

    @Test
    public void constructor_oversizedOffscreenSettings_fitsActualPrimaryScreen() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            MainWindow window = createWindow(new GuiSettings(bounds.getWidth() * 2, bounds.getHeight() * 2,
                    Integer.MAX_VALUE, Integer.MAX_VALUE));
            assertGeometry(window.getPrimaryStage(), bounds.getWidth(), bounds.getHeight(),
                    bounds.getMinX(), bounds.getMinY());
        });
    }

    private MainWindow createWindow(GuiSettings settings) {
        ModelManager model = new ModelManager();
        model.setGuiSettings(settings);
        List<Project> projects = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            projects.add(new Project(i, new ProjectTitle("Website launch " + i),
                    new ProjectDeadline("2026-10-30"), ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH));
        }
        model.addPerson(ALICE.withProjects(projects, 13));
        Path dataFile = testFolder.resolve("clients.json");
        LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
        return new MainWindow(new Stage(), logic, dataFile);
    }

    private void assertGeometry(Stage stage, double width, double height, double x, double y) {
        assertEquals(width, stage.getWidth());
        assertEquals(height, stage.getHeight());
        assertEquals(x, stage.getX());
        assertEquals(y, stage.getY());
    }
}
