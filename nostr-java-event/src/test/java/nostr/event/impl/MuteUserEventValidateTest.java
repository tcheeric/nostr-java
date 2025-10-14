package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MuteUserEventValidateTest {
  private static final String HEX_64_A = "a".repeat(64);
  private static final String HEX_64_B = "b".repeat(64);
  private static final String SIG_HEX = "c".repeat(128);

  private MuteUserEvent createValidEvent() {
    PublicKey pubKey = new PublicKey(HEX_64_A);
    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(new PublicKey(HEX_64_B)));
    MuteUserEvent event = new MuteUserEvent(pubKey, tags, "mute");
    event.setId(HEX_64_A);
    event.setSignature(Signature.fromString(SIG_HEX));
    event.setCreatedAt(Instant.now().getEpochSecond());
    return event;
  }

  // Valid mute user event should pass validation
  @Test
  public void testValidateSuccess() {
    MuteUserEvent event = createValidEvent();
    assertDoesNotThrow(event::validate);
  }

  // Validation fails when the pubkey tag is missing
  @Test
  public void testValidateMissingPubKeyTag() {
    MuteUserEvent event = createValidEvent();
    event.setTags(new ArrayList<>());
    assertThrows(AssertionError.class, event::validate);
  }

  // Validation fails if the event kind is incorrect
  @Test
  public void testValidateWrongKind() {
    MuteUserEvent event = createValidEvent();
    event.setKind(-1);
    assertThrows(AssertionError.class, event::validate);
  }

  // Retrieves the muted user's public key from tags
  @Test
  public void testGetMutedUser() {
    MuteUserEvent event = createValidEvent();
    assertEquals(HEX_64_B, event.getMutedUser().toString());
  }
}
