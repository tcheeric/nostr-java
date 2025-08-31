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
/**
 * NIP-44 helpers (Encrypted DM with XChaCha20). Encrypt/decrypt content and DM events.
 * Spec: https://github.com/nostr-protocol/nips/blob/master/44.md
 */
public class NIP44 extends EventNostr {

  /**
   * Encrypt a message using NIP-44 shared secret (XChaCha20-Poly1305) between sender and recipient.
   *
   * @param sender the identity of the sender (provides private key)
   * @param message the clear-text message
   * @param recipient the recipient public key
   * @return the encrypted content string
   */
  public static String encrypt(
      @NonNull Identity sender, @NonNull String message, @NonNull PublicKey recipient) {
    MessageCipher cipher =
        new MessageCipher44(sender.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.encrypt(message);
  }

  /**
   * Decrypt a NIP-44 encrypted content given the identity and peer public key.
   *
   * @param identity the identity performing decryption (sender or recipient)
   * @param encrypteEPessage the encrypted message content
   * @param recipient the peer public key (counterparty)
   * @return the clear-text message
   */
  public static String decrypt(
      @NonNull Identity identity, @NonNull String encrypteEPessage, @NonNull PublicKey recipient) {
    MessageCipher cipher =
        new MessageCipher44(identity.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.decrypt(encrypteEPessage);
  }

  /**
   * Decrypt a NIP-44 encrypted direct message event.
   *
   * @param recipient the identity performing decryption
   * @param event the encrypted event (DM)
   * @return the clear-text content
   */
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
