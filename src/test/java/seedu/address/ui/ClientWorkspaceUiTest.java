package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
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

/** Exercises the selected-client workspace using real commands and rendered project cards. */
public class ClientWorkspaceUiTest {

    @TempDir
    public Path testFolder;

    @Test
    public void workspace_projectsDetailsAndRefreshesAreAccessible() throws Exception {
        ModelManager model = new ModelManager();
        Project third = project(3, "Website launch", "2026-12-01", ProjectStatus.IN_PROGRESS,
                ProjectPriority.HIGH);
        Project first = project(1, "Initial draft", "2026-10-20", ProjectStatus.NOT_STARTED,
                ProjectPriority.LOW);
        Project second = project(2, "Client review", "2026-11-10", ProjectStatus.COMPLETED,
                ProjectPriority.MEDIUM);
        Person alice = new PersonBuilder().withName("Alice Example").withPhone("81111111")
                .withEmail("alice@example.com").withAddress("1 Main Street").withTags("vip")
                .withProjects(List.of(third, first, second), 4).build();
        Person benson = new PersonBuilder().withName("Benson Example").withPhone("82222222")
                .withEmail("benson@example.com").build();
        model.addPerson(alice);
        model.addPerson(benson);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("clients.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            try {
                window.fillInnerParts();
                window.show();
                window.getPrimaryStage().getScene().getRoot().applyCss();
                window.getPrimaryStage().getScene().getRoot().layout();

                TabPane tabs = lookup(window, "#clientTabs", TabPane.class);
                Tab projectsTab = tabs.getTabs().stream().filter(tab -> "Projects".equals(tab.getText()))
                        .findFirst().orElseThrow();
                Tab detailsTab = tabs.getTabs().stream().filter(tab -> "Details".equals(tab.getText()))
                        .findFirst().orElseThrow();
                VBox cards = lookup(window, "#projectCards", VBox.class);
                VBox emptyState = lookup(window, "#projectEmptyState", VBox.class);
                Label emptyTitle = lookup(window, "#projectEmptyTitle", Label.class);
                assertEquals(projectsTab, tabs.getSelectionModel().getSelectedItem());
                assertTrue(emptyState.isVisible());
                assertEquals("Select a client", emptyTitle.getText());
                assertTrue(cards.getChildren().isEmpty());
                assertTrue(window.getPrimaryStage().getMinWidth() >= 650);
                assertEquals(400, window.getPrimaryStage().getMinHeight());

                window.getPersonListPanel().selectClient(alice);
                window.getPrimaryStage().getScene().getRoot().applyCss();
                window.getPrimaryStage().getScene().getRoot().layout();
                assertEquals("Alice Example", lookup(window, "#clientName", Label.class).getText());
                assertEquals("81111111", lookup(window, "#clientPhone", Label.class).getText());
                assertEquals("alice@example.com", lookup(window, "#clientEmail", Label.class).getText());
                ScrollPane tagScroll = lookup(window, ".client-tag-scroll", ScrollPane.class);
                FlowPane clientTags = lookup(window, "#clientTags", FlowPane.class);
                Node tagViewportNode = tagScroll.lookup(".viewport");
                Bounds tagViewport = tagViewportNode.localToScene(tagViewportNode.getBoundsInLocal());
                assertTrue(tagViewport.getHeight() > 0,
                        "the tag row needs a visible scroll viewport");
                assertEquals(1, clientTags.getChildren().size());
                Label visibleTag = (Label) clientTags.getChildren().getFirst();
                Bounds tagBounds = visibleTag.localToScene(visibleTag.getBoundsInLocal());
                assertTrue(visibleTag.isVisible());
                assertTrue(tagBounds.getHeight() > 0);
                assertTrue(tagBounds.getMinY() >= tagViewport.getMinY());
                assertTrue(tagBounds.getMaxY() <= tagViewport.getMaxY(),
                        "tag pill should fit in viewport, pill=" + tagBounds + ", viewport=" + tagViewport);
                assertFalse(emptyState.isVisible());
                assertEquals(3, cards.getChildren().size());
                VBox firstCard = (VBox) cards.getChildren().get(0);
                assertEquals("#1  Initial draft", ((Label) firstCard.getChildren().get(0)).getText());
                assertEquals("Deadline: 2026-10-20", ((Label) firstCard.getChildren().get(1)).getText());
                FlowPane firstBadges = (FlowPane) firstCard.getChildren().get(2);
                assertEquals("not-started", ((Label) firstBadges.getChildren().get(0)).getText());
                assertEquals("low", ((Label) firstBadges.getChildren().get(1)).getText());
                assertTrue(firstCard.getAccessibleText().startsWith("#1  Initial draft"));
                assertTrue(cards.getChildren().get(1).getAccessibleText().startsWith("#2  Client review"));
                assertTrue(cards.getChildren().get(2).getAccessibleText().startsWith("#3  Website launch"));
                assertTrue(cards.getChildren().get(0).getAccessibleText().contains("Deadline: 2026-10-20"));
                assertTrue(cards.getChildren().get(0).getAccessibleText().contains("Status: not-started"));
                assertTrue(cards.getChildren().get(0).getAccessibleText().contains("Priority: low"));

                tabs.getSelectionModel().select(detailsTab);
                TextArea details = lookup(window, "#clientDetails", TextArea.class);
                assertFalse(details.isEditable());
                assertTrue(details.getText().contains("Name: Alice Example"));
                assertTrue(details.getText().contains("Address: 1 Main Street"));
                assertTrue(details.getText().contains("Projects:\n#1 | Initial draft"));
                details.selectRange(0, 4);
                assertEquals("Name", details.getSelectedText());

                TextField input = lookup(window, "#commandTextField", TextField.class);
                enter(input, "edit-project 1 3 status/completed priority/medium");
                assertTrue(cards.getChildren().get(2).getAccessibleText().contains("Status: completed"));
                assertTrue(cards.getChildren().get(2).getAccessibleText().contains("Priority: medium"));
                assertTrue(details.getText().contains("Status: completed | Priority: medium"));
                assertEquals(detailsTab, tabs.getSelectionModel().getSelectedItem());

                String beforeError = details.getText();
                enter(input, "edit-project 1 3 priority/urgent");
                assertEquals(beforeError, details.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                enter(input, "delete-project 1 1");
                assertEquals(2, cards.getChildren().size());
                assertTrue(details.getText().contains("Projects:\n#2 | Client review"));
                assertFalse(details.getText().contains("#1 | Initial draft"));

                tabs.getSelectionModel().select(projectsTab);
                window.getPrimaryStage().setWidth(650);
                window.getPrimaryStage().setHeight(600);
                window.getPrimaryStage().getScene().getRoot().applyCss();
                window.getPrimaryStage().getScene().getRoot().layout();
                assertTrue(window.getPrimaryStage().getWidth() >= 650);
                assertTrue(window.getPrimaryStage().getHeight() >= 600);
                assertVisibleWithinScene(tabs, window.getPrimaryStage().getScene());
                assertVisibleWithinScene(input, window.getPrimaryStage().getScene());
                Bounds cardsBounds = cards.localToScene(cards.getBoundsInLocal());
                assertTrue(cards.isVisible());
                assertTrue(cardsBounds.getWidth() > 0);
                assertTrue(cardsBounds.getHeight() > 0);
                assertTrue(cardsBounds.getMinY() < window.getPrimaryStage().getScene().getHeight());
                assertEquals(2, cards.getChildren().size());

                window.getPersonListPanel().selectClient(benson);
                assertEquals("Benson Example", lookup(window, "#clientName", Label.class).getText());
                assertTrue(emptyState.isVisible());
                assertEquals("No projects yet", emptyTitle.getText());
                assertTrue(cards.getChildren().isEmpty());
                tabs.getSelectionModel().select(detailsTab);
                assertFalse(details.getText().contains("Projects:"));
                assertEquals(detailsTab, tabs.getSelectionModel().getSelectedItem());

                window.getPrimaryStage().setWidth(850);
                window.getPrimaryStage().setHeight(700);
                window.getPrimaryStage().getScene().getRoot().layout();
                assertTrue(window.getPrimaryStage().getWidth() >= 650);
                assertTrue(window.getPrimaryStage().getHeight() >= 600);
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void workspace_longUnicodeFieldsAndManyTagsRemainUsableAtMinimumSize() throws Exception {
        ModelManager model = new ModelManager();
        String longName = ("李".repeat(7) + " ").repeat(9) + "王";
        String longEmail = "客户".repeat(100) + "@example.com";
        List<String> tags = IntStream.rangeClosed(1, 24).mapToObj(i -> "tag-" + i).toList();
        Person client = new PersonBuilder().withName(longName).withPhone("81111111")
                .withEmail(longEmail).withAddress("1 Main Street").withTags(tags.toArray(String[]::new))
                .withProjects(List.of(project(1, "資料整理", "2026-12-01", ProjectStatus.IN_PROGRESS,
                        ProjectPriority.HIGH)), 2).build();
        model.addPerson(client);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("long-client.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("long-client-preferences.json"))));
        MainWindow[] windowRef = new MainWindow[1];
        Node[] initialFocusRef = new Node[1];

        // Return to the test thread after queueing native stage changes so the next assertion runs after a pulse.
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            windowRef[0] = window;
            window.show();
            window.fillInnerParts();
            SplitPane splitPane = (SplitPane) window.getPrimaryStage().getScene().lookup(".workspace-split-pane");
            splitPane.setDividerPositions(0.95);
            window.getPrimaryStage().setWidth(650);
            window.getPrimaryStage().setHeight(600);
        });
        Thread.sleep(150);

        // Capture initial focus before selecting a client, then allow the header nodes to receive a layout pulse.
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = windowRef[0];
            initialFocusRef[0] = window.getPrimaryStage().getScene().getFocusOwner();
            window.getPersonListPanel().selectClient(client);
        });
        Thread.sleep(150);

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = windowRef[0];
            try {
                Scene scene = window.getPrimaryStage().getScene();
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                TextField input = lookup(window, "#commandTextField", TextField.class);
                assertEquals(input, initialFocusRef[0], "the command field should receive initial focus");
                assertTrue(window.getPrimaryStage().getWidth() >= 650);
                assertTrue(window.getPrimaryStage().getHeight() >= 600);
                assertTrue(window.getPrimaryStage().getWidth() <= 700);
                assertTrue(window.getPrimaryStage().getHeight() <= 650);
                assertTrue(scene.getWidth() > 0);
                assertTrue(scene.getHeight() > 0);

                SplitPane splitPane = (SplitPane) scene.lookup(".workspace-split-pane");
                assertTrue(splitPane.getDividerPositions()[0] < 0.7,
                        "the right divider should be constrained by the details pane minimum width");
                TabPane tabs = lookup(window, "#clientTabs", TabPane.class);
                VBox cards = lookup(window, "#projectCards", VBox.class);
                Label name = lookup(window, "#clientName", Label.class);
                Label email = lookup(window, "#clientEmail", Label.class);
                FlowPane tagPills = lookup(window, "#clientTags", FlowPane.class);
                ScrollPane tagScroll = lookup(window, ".client-tag-scroll", ScrollPane.class);
                assertVisibleWithinScene(tabs, scene);
                assertVisibleWithinScene(input, scene);
                Bounds cardsBounds = cards.localToScene(cards.getBoundsInLocal());
                assertTrue(cards.isVisible());
                assertTrue(cardsBounds.getWidth() > 0);
                assertTrue(cardsBounds.getHeight() > 0);
                assertEquals(longName, name.getText());
                assertEquals(longEmail, email.getText());
                assertEquals(24, tagPills.getChildren().size());
                assertTrue(name.getHeight() <= 70);
                assertTrue(email.getHeight() <= 36);
                Node tagViewportNode = tagScroll.lookup(".viewport");
                Bounds tagViewport = tagViewportNode.localToScene(tagViewportNode.getBoundsInLocal());
                Bounds firstTagBounds = tagPills.getChildren().getFirst()
                        .localToScene(tagPills.getChildren().getFirst().getBoundsInLocal());
                assertTrue(tagViewport.getHeight() > 0);
                assertTrue(firstTagBounds.getHeight() > 0);
                assertTrue(firstTagBounds.getMinY() >= tagViewport.getMinY());
                assertTrue(firstTagBounds.getMaxY() <= tagViewport.getMaxY());

                Tab detailsTab = tabs.getTabs().stream().filter(tab -> "Details".equals(tab.getText()))
                        .findFirst().orElseThrow();
                tabs.getSelectionModel().select(detailsTab);
                TextArea details = lookup(window, "#clientDetails", TextArea.class);
                assertTrue(details.getText().contains("Name: " + longName));
                assertTrue(details.getText().contains("Email: " + longEmail));
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    @Test
    public void workspace_compactWindowKeepsControlsAndBothTabsInsideScene() throws Exception {
        ModelManager model = new ModelManager();
        Person client = new PersonBuilder().withName("Compact View Client").withPhone("81111111")
                .withEmail("compact@example.com").withTags("vip", "design")
                .withProjects(List.of(project(1, "Compact project", "2026-12-01",
                        ProjectStatus.IN_PROGRESS, ProjectPriority.HIGH)), 2).build();
        model.addPerson(client);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("compact-client.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(testFolder.resolve("compact-client-preferences.json"))));
        MainWindow[] windowRef = new MainWindow[1];

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, storage.getAddressBookFilePath());
            windowRef[0] = window;
            window.show();
            window.fillInnerParts();
            window.getPersonListPanel().selectClient(client);
            window.getPrimaryStage().setMinHeight(400);
            window.getPrimaryStage().setWidth(853);
            window.getPrimaryStage().setHeight(400);
        });
        Thread.sleep(150);

        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow window = windowRef[0];
            try {
                Scene scene = window.getPrimaryStage().getScene();
                scene.getRoot().applyCss();
                scene.getRoot().layout();
                assertTrue(window.getPrimaryStage().getWidth() >= 853);
                assertTrue(window.getPrimaryStage().getWidth() <= 900);
                assertTrue(window.getPrimaryStage().getHeight() >= 400);
                assertTrue(window.getPrimaryStage().getHeight() <= 450);

                TabPane tabs = lookup(window, "#clientTabs", TabPane.class);
                TextField input = lookup(window, "#commandTextField", TextField.class);
                TextArea feedback = lookup(window, "#resultDisplay", TextArea.class);
                Node commandPlaceholder = lookup(window, "#commandBoxPlaceholder", Node.class);
                Node footer = lookup(window, "#statusbarPlaceholder", Node.class);
                Node footerContent = scene.lookup(".status-bar");
                ScrollPane projects = lookup(window, ".project-scroll", ScrollPane.class);
                assertVisibleWithinScene(tabs, scene);
                assertVisibleWithinScene(input, scene);
                assertVisibleWithinScene(feedback, scene);
                assertVisibleWithinScene(footer, scene);
                assertVisibleWithinScene(footerContent, scene);
                Node projectViewport = projects.lookup(".viewport");
                assertTrue(projectViewport.isVisible());
                assertTrue(tabs.getHeight() >= 80, "both workspace tabs need a usable viewport");
                assertTrue(projectViewport.localToScene(projectViewport.getBoundsInLocal()).getHeight() >= 32,
                        "the Projects tab needs a visible card viewport");
                Bounds tabsBounds = tabs.localToScene(tabs.getBoundsInLocal());
                Bounds commandBounds = commandPlaceholder.localToScene(commandPlaceholder.getBoundsInLocal());
                assertTrue(tabsBounds.getMaxY() <= commandBounds.getMinY(),
                        "the workspace must end above the command dock");

                Tab detailsTab = tabs.getTabs().stream().filter(tab -> "Details".equals(tab.getText()))
                        .findFirst().orElseThrow();
                tabs.getSelectionModel().select(detailsTab);
                TextArea details = lookup(window, "#clientDetails", TextArea.class);
                assertVisibleWithinScene(details, scene);
                assertTrue(details.getText().contains("Name: Compact View Client"));
                assertTrue(details.getText().contains("Projects:\n#1 | Compact project"));
            } finally {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
            }
        });
    }

    private Project project(int id, String title, String deadline, ProjectStatus status, ProjectPriority priority) {
        return new Project(id, new ProjectTitle(title), new ProjectDeadline(deadline), status, priority);
    }

    private <T extends Node> T lookup(MainWindow window, String selector, Class<T> type) {
        return type.cast(window.getPrimaryStage().getScene().lookup(selector));
    }

    private void assertVisibleWithinScene(Node node, Scene scene) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        Node parent = node.getParent();
        String parentInfo = parent == null ? "none" : parent.getClass().getSimpleName() + "#" + parent.getId()
                + " styles=" + parent.getStyleClass() + " bounds="
                + parent.localToScene(parent.getBoundsInLocal());
        Window window = scene.getWindow();
        String windowSize = window == null ? "unattached" : window.getWidth() + "x" + window.getHeight();
        String diagnostic = "node=" + node.getClass().getSimpleName() + "#" + node.getId()
                + " styles=" + node.getStyleClass() + " localBounds=" + node.getBoundsInLocal()
                + " sceneBounds=" + bounds + " parent=" + parentInfo
                + " sceneSize=" + scene.getWidth() + "x" + scene.getHeight()
                + " windowSize=" + windowSize;
        assertTrue(node.isVisible(), "not visible: " + diagnostic);
        assertTrue(bounds.getWidth() > 0, "zero scene width: " + diagnostic);
        assertTrue(bounds.getHeight() > 0, "zero scene height: " + diagnostic);
        assertTrue(bounds.getMinX() >= 0, "left of scene: " + diagnostic);
        assertTrue(bounds.getMinY() >= 0, "above scene: " + diagnostic);
        assertTrue(bounds.getMaxX() <= scene.getWidth(), "right of scene: " + diagnostic);
        assertTrue(bounds.getMaxY() <= scene.getHeight(), "below scene: " + diagnostic);
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
