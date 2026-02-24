package nostr.client.springwebsocket;

import java.io.IOException;

/**
 * Thrown when a relay does not respond within the configured timeout.
 */
public class RelayTimeoutException extends IOException {

  private final long timeoutMs;

  public RelayTimeoutException(long timeoutMs) {
    super("Timed out waiting for relay response after " + timeoutMs + "ms");
    this.timeoutMs = timeoutMs;
  }

  public long getTimeoutMs() {
    return timeoutMs;
  }
}
