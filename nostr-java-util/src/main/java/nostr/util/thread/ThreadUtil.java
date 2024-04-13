package nostr.util.thread;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.context.Context;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@AllArgsConstructor
@Builder
@Log
public class ThreadUtil<T extends Task> {

    private final T task;

    @Builder.Default
    private int timeoutSeconds = TIMEOUT_SECONDS;

    @Builder.Default
    private boolean blocking = false;

    public static final int TIMEOUT_SECONDS = 5;

    public ThreadUtil(@NonNull T task) {
        this.task = task;
        this.blocking = false;
        this.timeoutSeconds = TIMEOUT_SECONDS;
    }

    public void run(@NonNull Context context) throws TimeoutException {
        log.log(Level.FINE, "Executing thread on {0}...", task);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        Future<?> futureTask = threadPool.submit(() -> {
            task.execute(context);
        });

        if (blocking) {
            try {
                log.log(Level.FINE, "Waiting for thread to complete... ");
                futureTask.get(timeoutSeconds, TimeUnit.SECONDS); // Wait for the thread to complete
                log.log(Level.FINE, "Thread execution completed!");
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                threadPool.shutdown();
            }
        }
    }
}
