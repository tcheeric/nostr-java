package nostr.event.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.tag.EventTag;
import org.junit.jupiter.api.Test;

public class DeletionEventValidateTest {
  private static final String HEX_64_A = "a".repeat(64);
  private static final String HEX_64_B = "b".repeat(64);
  private static final String SIG_HEX = "c".repeat(128);

  private DeletionEvent createValidEvent() {
    PublicKey pubKey = new PublicKey(HEX_64_A);
    List<BaseTag> tags = new ArrayList<>();
    tags.add(new EventTag(HEX_64_B));
    tags.add(BaseTag.create("k", "1"));
    DeletionEvent event = new DeletionEvent(pubKey, tags);
    event.setId(HEX_64_A);
    event.setSignature(Signature.fromString(SIG_HEX));
    event.setCreatedAt(Instant.now().getEpochSecond());
    return event;
  }

  // Valid deletion event with required tags passes validation
  @Test
  public void testValidateSuccess() {
    DeletionEvent event = createValidEvent();
    assertDoesNotThrow(event::validate);
  }

  // Validation fails when event or author tag is missing
  @Test
  public void testValidateMissingEventOrAuthorTag() {
    DeletionEvent event = createValidEvent();
    List<BaseTag> tags = new ArrayList<>();
    tags.add(BaseTag.create("k", "1"));
    event.setTags(tags);
    assertThrows(AssertionError.class, event::validate);
  }

  // Validation fails when the kind tag is absent
  @Test
  public void testValidateMissingKindTag() {
    DeletionEvent event = createValidEvent();
    List<BaseTag> tags = new ArrayList<>();
    tags.add(new EventTag(HEX_64_B));
    event.setTags(tags);
    assertThrows(AssertionError.class, event::validate);
  }

  // Validation fails if the event kind is incorrect
  @Test
  public void testValidateWrongKind() {
    DeletionEvent event = createValidEvent();
    event.setKind(-1);
    assertThrows(AssertionError.class, event::validate);
  }
}
