package nostr.event.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.tag.EventTag;
import org.junit.jupiter.api.Test;

public class ReactionEventValidateTest {
  private static final String HEX_64_A = "a".repeat(64);
  private static final String HEX_64_B = "b".repeat(64);
  private static final String SIG_HEX = "c".repeat(128);

  private ReactionEvent createValidEvent() {
    PublicKey pubKey = new PublicKey(HEX_64_A);
    List<BaseTag> tags = new ArrayList<>();
    tags.add(new EventTag(HEX_64_B));
    ReactionEvent event = new ReactionEvent(pubKey, tags, "+");
    event.setId(HEX_64_A);
    event.setSignature(Signature.fromString(SIG_HEX));
    event.setCreatedAt(Instant.now().getEpochSecond());
    return event;
  }

  // Valid reaction event should pass validation
  @Test
  public void testValidateSuccess() {
    ReactionEvent event = createValidEvent();
    assertDoesNotThrow(event::validate);
  }

  // Validation fails when the required event tag is missing
  @Test
  public void testValidateMissingEventTag() {
    ReactionEvent event = createValidEvent();
    event.setTags(new ArrayList<>());
    assertThrows(AssertionError.class, event::validate);
  }

  // Validation fails if the event kind is incorrect
  @Test
  public void testValidateWrongKind() {
    ReactionEvent event = createValidEvent();
    event.setKind(-1);
    assertThrows(AssertionError.class, event::validate);
  }

  // Retrieves the ID of the reacted event from tags
  @Test
  public void testGetReactedEventId() {
    ReactionEvent event = createValidEvent();
    assertEquals(HEX_64_B, event.getReactedEventId());
  }
}
