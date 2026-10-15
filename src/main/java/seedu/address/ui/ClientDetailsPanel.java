package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.person.Person;

/**
 * Scrollable, read-only details for the selected client, including saved projects.
 */
public class ClientDetailsPanel extends UiPart<Region> {

    public static final String MESSAGE_EMPTY = "Enter view INDEX or select a client to see their details.";
    private static final String FXML = "ClientDetailsPanel.fxml";

    @FXML
    private TextArea clientDetails;

    /**
     * Creates a details panel showing instructions until a client is selected.
     */
    public ClientDetailsPanel() {
        super(FXML);
        showClient(null);
    }

    /**
     * Shows the selected client's current details, or instructions when selection is cleared.
     */
    public void showClient(Person client) {
        String details = client == null ? MESSAGE_EMPTY : ViewCommand.formatDetails(client);
        if (client != null && client.getProjects().isEmpty()) {
            details += "\nProjects: No projects yet.";
        }
        clientDetails.setText(details);
        clientDetails.positionCaret(0);
    }
}
