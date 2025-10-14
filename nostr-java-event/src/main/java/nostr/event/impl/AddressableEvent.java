package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Addressable Events")
@NoArgsConstructor
public class AddressableEvent extends NIP01Event {

  public AddressableEvent(PublicKey pubKey, Integer kind, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags, content);
  }

  /**
   * Validates that the event kind is within the addressable event range.
   *
   * <p>Per NIP-01, addressable events (also called parameterized replaceable events) must have
   * kinds in the range [30000, 40000). These events are replaceable and addressable via the
   * combination of kind, pubkey, and 'd' tag.
   *
   * @throws AssertionError if kind is not in the valid range [30000, 40000)
   */
  @Override
  public void validateKind() {
    super.validateKind();

    Integer n = getKind();
    // NIP-01: Addressable events must be in range [30000, 40000)
    if (n >= 30_000 && n < 40_000) {
      return; // Valid addressable event kind
    }

    throw new AssertionError(
        String.format(
            "Invalid kind value %d. Addressable events must be in range [30000, 40000).", n),
        null);
  }
}
