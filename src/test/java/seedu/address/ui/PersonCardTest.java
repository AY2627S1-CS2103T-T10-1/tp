package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    @BeforeAll
    public static void initializeToolkit() {
        try {
            Platform.startup(() -> Platform.setImplicitExit(false));
        } catch (IllegalStateException e) {
            // Other UI tests may have already initialized JavaFX.
        }
    }

    @Test
    public void constructor_nonEmptyRemark_displaysRemark() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            var person = new PersonBuilder().withRemark("Likes baseball").build();
            Label remark = (Label) new PersonCard(person, 1).getRoot().lookup("#remark");
            assertEquals("Likes baseball", remark.getText());
            assertTrue(remark.isVisible());
            assertTrue(remark.isManaged());
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_emptyRemark_hidesBlankLine() throws Exception {
        FutureTask<Void> check = new FutureTask<>(() -> {
            Label remark = (Label) new PersonCard(new PersonBuilder().build(), 1).getRoot().lookup("#remark");
            assertEquals("", remark.getText());
            assertFalse(remark.isVisible());
            assertFalse(remark.isManaged());
            return null;
        });
        Platform.runLater(check);
        check.get(10, TimeUnit.SECONDS);
    }
}
