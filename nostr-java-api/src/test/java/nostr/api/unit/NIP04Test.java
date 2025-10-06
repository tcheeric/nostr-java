package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nostr.api.NIP04;
import nostr.base.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class NIP04Test {

  @Test
  public void testCreateAndDecryptDirectMessage() {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();
    String content = "hello";

    NIP04 nip04 = new NIP04(sender, recipient.getPublicKey());
    nip04.createDirectMessageEvent(content);

    GenericEvent event = nip04.getEvent();
    assertEquals(Kind.ENCRYPTED_DIRECT_MESSAGE.getValue(), event.getKind());
    assertTrue(event.getTags().stream().anyMatch(t -> t instanceof PubKeyTag));

    String decrypted = NIP04.decrypt(recipient, event);
    assertEquals(content, decrypted);
  }
}
