package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EphemeralEventValidateTest {
  private static final String HEX_64 = "a".repeat(64);
  private static final String SIG_HEX = "b".repeat(128);

  private EphemeralEvent createEvent(int kind) {
    EphemeralEvent event =
        new EphemeralEvent(new PublicKey(HEX_64), kind, new ArrayList<BaseTag>(), "");
    event.setId(HEX_64);
    event.setCreatedAt(Instant.now().getEpochSecond());
    event.setSignature(Signature.fromString(SIG_HEX));
    return event;
  }

  // Kind within the 20000-29999 range validates successfully
  @Test
  public void testValidateKindSuccess() {
    EphemeralEvent event = createEvent(20000);
    assertDoesNotThrow(event::validate);
  }

  // Kind outside the 20000-29999 range fails validation
  @Test
  public void testValidateKindFailure() {
    EphemeralEvent event = createEvent(1000);
    assertThrows(AssertionError.class, event::validate);
  }
}
