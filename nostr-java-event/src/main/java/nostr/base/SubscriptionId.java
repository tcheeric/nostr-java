package nostr.base;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Strongly typed wrapper around subscription identifiers to avoid primitive obsession.
 */
@EqualsAndHashCode
public final class SubscriptionId {

  private final String value;

  private SubscriptionId(String value) {
    this.value = value;
  }

  public static SubscriptionId of(@NonNull String value) {
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Subscription id must not be blank");
    }
    return new SubscriptionId(trimmed);
  }

  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
