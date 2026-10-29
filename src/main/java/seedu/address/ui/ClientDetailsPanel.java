package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.person.Person;
import seedu.address.model.project.Project;
import seedu.address.model.project.ProjectPriority;
import seedu.address.model.project.ProjectStatus;
import seedu.address.model.tag.Tag;

/**
 * Workspace for the selected client and their saved projects.
 */
public class ClientDetailsPanel extends UiPart<Region> {

    public static final String MESSAGE_EMPTY = "Enter view INDEX or select a client to see their details.";
    private static final String FXML = "ClientDetailsPanel.fxml";

    @FXML
    private Label clientAvatar;
    @FXML
    private Label clientName;
    @FXML
    private Label clientPhone;
    @FXML
    private Label clientEmail;
    @FXML
    private FlowPane clientTags;
    @FXML
    private ScrollPane clientTagScroll;
    @FXML
    private TabPane clientTabs;
    @FXML
    private Tab projectsTab;
    @FXML
    private Tab detailsTab;
    @FXML
    private VBox projectCards;
    @FXML
    private VBox projectEmptyState;
    @FXML
    private Label projectEmptyTitle;
    @FXML
    private Label projectEmptyDescription;
    @FXML
    private TextArea clientDetails;

    /**
     * Creates a workspace with no client selected and the Projects tab open.
     */
    public ClientDetailsPanel() {
        super(FXML);
        clientTabs.getSelectionModel().select(projectsTab);
        showClient(null);
    }

    /**
     * Refreshes the selected client's header, projects and copyable text details without changing the active tab.
     */
    public void showClient(Person client) {
        boolean hasTags = client != null && !client.getTags().isEmpty();
        clientTagScroll.setVisible(hasTags);
        clientTagScroll.setManaged(hasTags);
        if (client == null) {
            clientAvatar.setText("?");
            clientName.setText("No client selected");
            clientPhone.setText("");
            clientEmail.setText("");
            clientTags.getChildren().clear();
            clientDetails.setText(MESSAGE_EMPTY);
            projectCards.getChildren().clear();
            projectEmptyTitle.setText("Select a client");
            projectEmptyDescription.setText("Choose a client from the list to see their projects.");
            showProjectEmptyState(true);
            return;
        }

        String name = client.getName().fullName;
        clientAvatar.setText(firstCharacter(name));
        clientName.setText(name);
        clientPhone.setText(client.getPhone().value);
        clientEmail.setText(client.getEmail().value);
        clientTags.getChildren().setAll(client.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(this::createTagPill)
                .toList());
        clientDetails.setText(ViewCommand.formatDetails(client));
        clientDetails.positionCaret(0);

        projectCards.getChildren().setAll(client.getProjects().stream()
                .sorted(Comparator.comparingInt(Project::getId))
                .map(this::createProjectCard)
                .toList());
        projectEmptyTitle.setText("No projects yet");
        projectEmptyDescription.setText("Projects added for this client will appear here.");
        showProjectEmptyState(client.getProjects().isEmpty());
    }

    private String firstCharacter(String text) {
        int firstCodePoint = text.codePointAt(0);
        return new String(Character.toChars(firstCodePoint)).toUpperCase(java.util.Locale.ROOT);
    }

    private Label createTagPill(Tag tag) {
        Label pill = new Label(tag.tagName);
        pill.getStyleClass().add("tag-pill");
        pill.setWrapText(true);
        return pill;
    }

    private VBox createProjectCard(Project project) {
        Label title = new Label("#" + project.getId() + "  " + project.getTitle().value);
        title.getStyleClass().add("project-title");
        title.setWrapText(true);

        Label deadline = new Label("Deadline: " + project.getDeadline());
        deadline.getStyleClass().add("project-meta");
        deadline.setWrapText(true);

        Label status = new Label(project.getStatus().toString());
        status.getStyleClass().addAll("status-pill", statusStyle(project.getStatus()));

        Label priority = new Label(project.getPriority().toString());
        priority.getStyleClass().addAll("priority-pill", priorityStyle(project.getPriority()));

        FlowPane badges = new FlowPane(8, 6, status, priority);
        VBox card = new VBox(9, title, deadline, badges);
        card.getStyleClass().add("project-card");
        card.setAccessibleText(title.getText() + ", Deadline: " + project.getDeadline()
                + ", Status: " + project.getStatus() + ", Priority: " + project.getPriority());
        return card;
    }

    private String statusStyle(ProjectStatus status) {
        return switch (status) {
            case NOT_STARTED -> "status-not-started";
            case IN_PROGRESS -> "status-in-progress";
            case COMPLETED -> "status-completed";
        };
    }

    private String priorityStyle(ProjectPriority priority) {
        return switch (priority) {
            case LOW -> "priority-low";
            case MEDIUM -> "priority-medium";
            case HIGH -> "priority-high";
        };
    }

    private void showProjectEmptyState(boolean show) {
        projectEmptyState.setVisible(show);
        projectEmptyState.setManaged(show);
    }
}
