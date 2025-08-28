package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import nostr.api.NIP44;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class NIP44Test {

  @Test
  public void testEncryptDecrypt() {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();
    String message = "hello";

    String encrypted = NIP44.encrypt(sender, message, recipient.getPublicKey());
    String decrypted = NIP44.decrypt(recipient, encrypted, sender.getPublicKey());
    assertEquals(message, decrypted);
  }

  @Test
  public void testDecryptEvent() {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();

    String content = "msg";
    String enc = NIP44.encrypt(sender, content, recipient.getPublicKey());
    GenericEvent event =
        new GenericEvent(
            sender.getPublicKey(), 1050, List.of(new PubKeyTag(recipient.getPublicKey())), enc);

    String dec = NIP44.decrypt(recipient, event);
    assertEquals(content, dec);
  }
}
