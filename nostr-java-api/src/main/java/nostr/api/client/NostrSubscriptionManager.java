package nostr.api.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.NonNull;
import nostr.event.filter.Filters;

/**
 * Manages subscription lifecycles across multiple relay handlers.
 */
public final class NostrSubscriptionManager {

  private final NostrRelayRegistry relayRegistry;

  public NostrSubscriptionManager(NostrRelayRegistry relayRegistry) {
    this.relayRegistry = relayRegistry;
  }

  public AutoCloseable subscribe(
      @NonNull Filters filters,
      @NonNull String subscriptionId,
      @NonNull Consumer<String> listener,
      @NonNull Consumer<Throwable> errorConsumer) {
    List<AutoCloseable> handles = new ArrayList<>();
    try {
      for (var handler : relayRegistry.baseHandlers()) {
        AutoCloseable handle = handler.subscribe(filters, subscriptionId, listener, errorConsumer);
        handles.add(handle);
      }
    } catch (RuntimeException e) {
      closeQuietly(handles, errorConsumer);
      throw e;
    }

    return () -> closeHandles(handles, errorConsumer);
  }

  private void closeHandles(List<AutoCloseable> handles, Consumer<Throwable> errorConsumer)
      throws IOException {
    IOException ioFailure = null;
    Exception nonIoFailure = null;
    for (AutoCloseable handle : handles) {
      try {
        handle.close();
      } catch (IOException e) {
        errorConsumer.accept(e);
        if (ioFailure == null) {
          ioFailure = e;
        }
      } catch (Exception e) {
        errorConsumer.accept(e);
        nonIoFailure = e;
      }
    }

    if (ioFailure != null) {
      throw ioFailure;
    }
    if (nonIoFailure != null) {
      throw new IOException("Failed to close subscription", nonIoFailure);
    }
  }

  private void closeQuietly(List<AutoCloseable> handles, Consumer<Throwable> errorConsumer) {
    for (AutoCloseable handle : handles) {
      try {
        handle.close();
      } catch (Exception closeEx) {
        errorConsumer.accept(closeEx);
      }
    }
  }
}
