package seedu.address.testutil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Runs UI assertions on the JavaFX application thread.
 */
public class JavaFxTestUtil {

    private static final CompletableFuture<Void> STARTED = new CompletableFuture<>();

    static {
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            STARTED.complete(null);
        });
    }

    /**
     * Runs the given action and forwards any assertion failure to the test thread.
     */
    public static void runOnFxThread(Runnable action) throws Exception {
        STARTED.get(30, TimeUnit.SECONDS);
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }
}
