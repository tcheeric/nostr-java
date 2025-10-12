package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChannelMessageEventValidateTest {
  private static final String HEX_64_A = "a".repeat(64);
  private static final String HEX_64_B = "b".repeat(64);
  private static final String SIG_HEX = "c".repeat(128);
  private static final String CHANNEL_JSON =
      "{\"name\":\"chan\",\"about\":\"desc\",\"picture\":\"http://example.com/img.png\"}";

  private ChannelCreateEvent createRootEvent() {
    ChannelCreateEvent root = new ChannelCreateEvent(new PublicKey(HEX_64_A), CHANNEL_JSON);
    root.setId(HEX_64_B);
    root.setCreatedAt(Instant.now().getEpochSecond());
    root.setSignature(Signature.fromString(SIG_HEX));
    return root;
  }

  private ChannelMessageEvent createValidEvent() {
    ChannelCreateEvent root = createRootEvent();
    ChannelMessageEvent event = new ChannelMessageEvent(new PublicKey(HEX_64_A), root, "hi", null);
    event.setId(HEX_64_A);
    event.setCreatedAt(Instant.now().getEpochSecond());
    event.setSignature(Signature.fromString(SIG_HEX));
    return event;
  }

  // Channel message referencing its root event validates successfully
  @Test
  public void testValidateSuccess() {
    ChannelMessageEvent event = createValidEvent();
    assertDoesNotThrow(event::validate);
  }

  // Missing root event tag results in validation failure
  @Test
  public void testValidateMissingRootTag() {
    ChannelMessageEvent event =
        new ChannelMessageEvent(new PublicKey(HEX_64_A), new ArrayList<BaseTag>(), "hi");
    event.setId(HEX_64_A);
    event.setCreatedAt(Instant.now().getEpochSecond());
    event.setSignature(Signature.fromString(SIG_HEX));
    assertThrows(AssertionError.class, event::validate);
  }

  // Wrong kind value triggers validation error
  @Test
  public void testValidateWrongKind() {
    ChannelMessageEvent event = createValidEvent();
    event.setKind(-1);
    assertThrows(AssertionError.class, event::validate);
  }
}
