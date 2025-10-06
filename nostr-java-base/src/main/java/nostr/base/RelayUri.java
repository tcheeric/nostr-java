package nostr.base;

import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Value object that encapsulates validation of relay URIs.
 */
@EqualsAndHashCode
public final class RelayUri {

  private final String value;

  public RelayUri(@NonNull String value) {
    try {
      URI uri = URI.create(value);
      String scheme = uri.getScheme();
      if (scheme == null || !("ws".equalsIgnoreCase(scheme) || "wss".equalsIgnoreCase(scheme))) {
        throw new IllegalArgumentException("Relay URI must use ws or wss scheme");
      }
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid relay URI: " + value, ex);
    }
    this.value = value;
  }

  public String value() {
    return value;
  }

  public URI toUri() {
    return URI.create(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
