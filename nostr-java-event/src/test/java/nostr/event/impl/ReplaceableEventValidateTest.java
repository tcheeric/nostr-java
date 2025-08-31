package nostr.event.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import org.junit.jupiter.api.Test;

public class ReplaceableEventValidateTest {
  private static final String HEX_64_A = "a".repeat(64);
  private static final String SIG_HEX = "c".repeat(128);

  private ReplaceableEvent createEventWithKind(int kind) {
    PublicKey pubKey = new PublicKey(HEX_64_A);
    List<BaseTag> tags = new ArrayList<>();
    ReplaceableEvent event = new ReplaceableEvent(pubKey, kind, tags, "");
    event.setId(HEX_64_A);
    event.setSignature(Signature.fromString(SIG_HEX));
    event.setCreatedAt(Instant.now().getEpochSecond());
    return event;
  }

  // Validation succeeds when kind falls within replaceable range
  @Test
  public void testValidateKindInRange() {
    ReplaceableEvent event = createEventWithKind(10_000);
    assertDoesNotThrow(event::validate);
  }

  // Kind zero is allowed for replaceable events
  @Test
  public void testValidateKindZero() {
    ReplaceableEvent event = createEventWithKind(0);
    assertDoesNotThrow(event::validate);
  }

  // Kind three is permitted under replaceable rules
  @Test
  public void testValidateKindThree() {
    ReplaceableEvent event = createEventWithKind(3);
    assertDoesNotThrow(event::validate);
  }

  // Validation fails when kind is outside the replaceable range
  @Test
  public void testValidateKindInvalid() {
    ReplaceableEvent event = createEventWithKind(9_999);
    assertThrows(AssertionError.class, event::validate);
  }
}
