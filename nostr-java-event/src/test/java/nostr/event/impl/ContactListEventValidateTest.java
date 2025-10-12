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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContactListEventValidateTest {
  private static final String HEX_64_A =
      "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
  private static final String HEX_64_B =
      "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
  private static final String SIG_HEX = "c".repeat(128);

  private ContactListEvent createValidEvent() {
    PublicKey pubKey = new PublicKey(HEX_64_A);
    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(new PublicKey(HEX_64_B)));
    ContactListEvent event = new ContactListEvent(pubKey, tags);
    event.setId(HEX_64_A);
    event.setSignature(Signature.fromString(SIG_HEX));
    event.setCreatedAt(Instant.now().getEpochSecond());
    return event;
  }

  @Test
  public void testValidateSuccess() {
    ContactListEvent event = createValidEvent();
    assertDoesNotThrow(event::validate);
  }

  @Test
  public void testValidateMissingPTag() {
    ContactListEvent event = createValidEvent();
    event.setTags(new ArrayList<>());
    assertThrows(AssertionError.class, event::validate);
  }

  @Test
  public void testValidateWrongKind() {
    ContactListEvent event = createValidEvent();
    event.setKind(-1);
    assertThrows(AssertionError.class, event::validate);
  }

  @Test
  public void testValidateInvalidContent() {
    ContactListEvent event = createValidEvent();
    event.setContent(null);
    assertThrows(AssertionError.class, event::validate);
  }
}
