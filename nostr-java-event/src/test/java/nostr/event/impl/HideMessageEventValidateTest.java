package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.tag.EventTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HideMessageEventValidateTest {
  private static final String HEX_64_A = "a".repeat(64);
  private static final String HEX_64_B = "b".repeat(64);
  private static final String SIG_HEX = "c".repeat(128);

  private HideMessageEvent createValidEvent() {
    List<BaseTag> tags = new ArrayList<>();
    tags.add(new EventTag(HEX_64_B));
    HideMessageEvent event = new HideMessageEvent(new PublicKey(HEX_64_A), tags, "");
    event.setId(HEX_64_A);
    event.setCreatedAt(Instant.now().getEpochSecond());
    event.setSignature(Signature.fromString(SIG_HEX));
    return event;
  }

  // Hide message event with at least one event tag validates successfully
  @Test
  public void testValidateSuccess() {
    HideMessageEvent event = createValidEvent();
    assertDoesNotThrow(event::validate);
  }

  // Missing event tag causes validation to fail
  @Test
  public void testValidateMissingEventTag() {
    HideMessageEvent event = createValidEvent();
    event.setTags(new ArrayList<>());
    assertThrows(AssertionError.class, event::validate);
  }

  // Wrong kind value triggers validation error
  @Test
  public void testValidateWrongKind() {
    HideMessageEvent event = createValidEvent();
    event.setKind(-1);
    assertThrows(AssertionError.class, event::validate);
  }
}
