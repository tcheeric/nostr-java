package nostr.util.thread;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.context.Context;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

@AllArgsConstructor
@Builder
@Log
public class ThreadUtil<T extends Task> {

    private final T task;

    @Builder.Default
    private final boolean wait = false;

    @Builder.Default
    private final int timeout = TIMEOUT_SECONDS;

    public static final int TIMEOUT_SECONDS = 5;

    public void run(@NonNull Context context) {
        log.log(Level.INFO, "Executing thread on {0}...", task);
        ExecutorService executor = newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            task.execute(context);
        });

        if (wait) {
            try {
                log.log(Level.INFO, "Waiting for thread to complete... ");
                future.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                log.log(Level.WARNING, "Timeout occurred while waiting for thread to complete! Aborting...");
                throw new RuntimeException(e);
            }
            log.log(Level.INFO, "Thread execution completed!");
        }
    }
}
