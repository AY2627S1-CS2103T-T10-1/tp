package seedu.address.testutil;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import seedu.address.ui.MainWindow;

/** Helpers for navigating the client workspace in JavaFX UI tests. */
public final class ClientWorkspaceTestUtil {

    private ClientWorkspaceTestUtil() {}

    /** Selects the copyable details view while keeping the workspace available for assertions. */
    public static void selectDetailsTab(MainWindow window) {
        TabPane tabs = (TabPane) window.getPrimaryStage().getScene().lookup("#clientTabs");
        Tab details = tabs.getTabs().stream().filter(tab -> "Details".equals(tab.getText())).findFirst().orElseThrow();
        tabs.getSelectionModel().select(details);
    }
}
