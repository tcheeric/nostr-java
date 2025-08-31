package nostr.event.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import org.junit.jupiter.api.Test;

public class AddressableEventValidateTest {
  private static final String HEX_64 = "a".repeat(64);
  private static final String SIG_HEX = "b".repeat(128);

  private AddressableEvent createEvent(int kind) {
    AddressableEvent event =
        new AddressableEvent(new PublicKey(HEX_64), kind, new ArrayList<BaseTag>(), "");
    event.setId(HEX_64);
    event.setCreatedAt(Instant.now().getEpochSecond());
    event.setSignature(Signature.fromString(SIG_HEX));
    return event;
  }

  // Valid kind within the 30000-39999 range passes validation
  @Test
  public void testValidateKindSuccess() {
    AddressableEvent event = createEvent(30000);
    assertDoesNotThrow(event::validate);
  }

  // Kind outside the allowed range triggers validation failure
  @Test
  public void testValidateKindFailure() {
    AddressableEvent event = createEvent(1000);
    assertThrows(AssertionError.class, event::validate);
  }
}
