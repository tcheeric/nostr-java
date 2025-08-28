package nostr.event.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import org.junit.jupiter.api.Test;

public class DirectMessageEventValidateTest {
  private static final String HEX_64_A = "a".repeat(64);
  private static final String HEX_64_B = "b".repeat(64);
  private static final String SIG_HEX = "c".repeat(128);

  private DirectMessageEvent createValidEvent() {
    DirectMessageEvent event =
        new DirectMessageEvent(new PublicKey(HEX_64_A), new PublicKey(HEX_64_B), "hello");
    event.setId(HEX_64_A);
    event.setCreatedAt(Instant.now().getEpochSecond());
    event.setSignature(Signature.fromString(SIG_HEX));
    return event;
  }

  private DirectMessageEvent createEventWithoutRecipient() {
    DirectMessageEvent event =
        new DirectMessageEvent(new PublicKey(HEX_64_A), new ArrayList<BaseTag>(), "hello");
    event.setId(HEX_64_A);
    event.setCreatedAt(Instant.now().getEpochSecond());
    event.setSignature(Signature.fromString(SIG_HEX));
    return event;
  }

  // Direct message with recipient tag validates successfully
  @Test
  public void testValidateSuccess() {
    DirectMessageEvent event = createValidEvent();
    assertDoesNotThrow(event::validate);
  }

  // Missing recipient public key tag causes validation failure
  @Test
  public void testValidateMissingRecipient() {
    DirectMessageEvent event = createEventWithoutRecipient();
    assertThrows(AssertionError.class, event::validate);
  }

  // Incorrect kind value is rejected during validation
  @Test
  public void testValidateWrongKind() {
    DirectMessageEvent event = createValidEvent();
    event.setKind(-1);
    assertThrows(AssertionError.class, event::validate);
  }
}
