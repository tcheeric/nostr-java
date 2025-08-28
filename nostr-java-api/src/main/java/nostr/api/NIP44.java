package nostr.api;

import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.encryption.MessageCipher;
import nostr.encryption.MessageCipher44;
import nostr.event.filter.Filterable;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

@Slf4j
public class NIP44 extends EventNostr {

  public static String encrypt(
      @NonNull Identity sender, @NonNull String message, @NonNull PublicKey recipient) {
    MessageCipher cipher =
        new MessageCipher44(sender.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.encrypt(message);
  }

  public static String decrypt(
      @NonNull Identity identity, @NonNull String encrypteEPessage, @NonNull PublicKey recipient) {
    MessageCipher cipher =
        new MessageCipher44(identity.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.decrypt(encrypteEPessage);
  }

  public static String decrypt(@NonNull Identity recipient, @NonNull GenericEvent event) {
    boolean rcptFlag = amITheRecipient(recipient, event);

    if (!rcptFlag) { // I am the message sender
      MessageCipher cipher =
          new MessageCipher44(
              recipient.getPrivateKey().getRawData(),
              Filterable.getTypeSpecificTags(PubKeyTag.class, event).stream()
                  .findFirst()
                  .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."))
                  .getPublicKey()
                  .getRawData());
      return cipher.decrypt(event.getContent());
    }

    // I am the message recipient
    var sender = event.getPubKey();
    log.debug("Decrypting message for {}", sender);
    MessageCipher cipher =
        new MessageCipher44(recipient.getPrivateKey().getRawData(), sender.getRawData());
    return cipher.decrypt(event.getContent());
  }

  private static boolean amITheRecipient(@NonNull Identity recipient, @NonNull GenericEvent event) {
    var pTag =
        event.getTags().stream()
            .filter(t -> t.getCode().equalsIgnoreCase("p"))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));

    if (Objects.equals(recipient.getPublicKey(), ((PubKeyTag) pTag).getPublicKey())) {
      return true;
    }

    if (Objects.equals(recipient.getPublicKey(), event.getPubKey())) {
      return false;
    }

    throw new RuntimeException("Unrelated event");
  }
}
