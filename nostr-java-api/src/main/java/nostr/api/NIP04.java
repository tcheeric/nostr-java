package nostr.api;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.PublicKey;
import nostr.config.Constants;
import nostr.encryption.MessageCipher;
import nostr.encryption.MessageCipher04;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.filter.Filterable;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

/**
 * NIP-04 helpers (Encrypted Direct Messages). Build and encrypt DM events.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/04.md">NIP-04</a>
 */
@Slf4j
public class NIP04 extends EventNostr {
  /**
   * Construct a NIP-04 helper for encrypting/sending DMs.
   *
   * @param sender the sender identity used for signing and encryption
   * @param recipient the recipient public key
   */
  public NIP04(@NonNull Identity sender, @NonNull PublicKey recipient) {
    setSender(sender);
    setRecipient(recipient);
  }

  /**
   * Create a NIP04 Encrypted Direct Message
   *
   * @param content the DM content in clear-text
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP04 createDirectMessageEvent(@NonNull String content) {
    log.debug("Creating direct message event");
    var encryptedContent = encrypt(getSender(), content, getRecipient());
    List<BaseTag> tags = List.of(new PubKeyTag(getRecipient()));

    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Constants.Kind.ENCRYPTED_DIRECT_MESSAGE, tags, encryptedContent)
            .create();
    this.updateEvent(genericEvent);

    return this;
  }

  /**
   * Encrypt the direct message
   *
   * @return the current instance with an encrypted message
   */
  public NIP04 encrypt() {
    encryptDirectMessage(getSender(), getEvent());
    return this;
  }

  /**
   * @param senderId the sender identity
   * @param message the message to be encrypted
   * @param recipient the recipient public key
   * @return the encrypted message
   */
  public static String encrypt(
      @NonNull Identity senderId, @NonNull String message, @NonNull PublicKey recipient) {
    log.debug("Encrypting message from {} to {}", senderId.getPublicKey(), recipient);
    MessageCipher cipher =
        new MessageCipher04(senderId.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.encrypt(message);
  }

  /**
   * Decrypt an encrypted direct message
   *
   * @param identity the sender identity
   * @param encryptedMessage the encrypted message
   * @param recipient the recipient public key
   * @return the DM content in clear-text
   */
  public static String decrypt(
      @NonNull Identity identity, @NonNull String encryptedMessage, @NonNull PublicKey recipient) {
    log.debug("Decrypting message for {}", identity.getPublicKey());
    MessageCipher cipher =
        new MessageCipher04(identity.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.decrypt(encryptedMessage);
  }

  private static void encryptDirectMessage(
      @NonNull Identity senderId, @NonNull GenericEvent directMessageEvent) {

    if (directMessageEvent.getKind() != Constants.Kind.ENCRYPTED_DIRECT_MESSAGE) {
      throw new IllegalArgumentException("Event is not an encrypted direct message");
    }

    GenericTag recipient =
        directMessageEvent.getTags().stream()
            .filter(t -> t.getCode().equalsIgnoreCase("p"))
            .map(tag -> (GenericTag) tag)
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));

    PubKeyTag pubKeyTag = PubKeyTag.updateFields(recipient);
    PublicKey rcptPublicKey = pubKeyTag.getPublicKey();
    MessageCipher cipher =
        new MessageCipher04(senderId.getPrivateKey().getRawData(), rcptPublicKey.getRawData());
    var encryptedContent = cipher.encrypt(directMessageEvent.getContent());
    directMessageEvent.setContent(encryptedContent);
  }

  /**
   * Decrypt an encrypted direct message
   *
   * @param rcptId the identity attempting to decrypt (recipient or sender)
   * @param event the encrypted direct message
   * @return the DM content in clear-text
   */
  public static String decrypt(@NonNull Identity rcptId, @NonNull GenericEvent event) {

    if (event.getKind() != Constants.Kind.ENCRYPTED_DIRECT_MESSAGE) {
      throw new IllegalArgumentException("Event is not an encrypted direct message");
    }

    PubKeyTag pTag =
        Filterable.getTypeSpecificTags(PubKeyTag.class, event).stream()
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));

    boolean rcptFlag = amITheRecipient(rcptId, event);

    if (!rcptFlag) { // I am the message sender
      log.debug("Decrypting own sent message");
      MessageCipher cipher =
          new MessageCipher04(
              rcptId.getPrivateKey().getRawData(), pTag.getPublicKey().getRawData());
      return cipher.decrypt(event.getContent());
    }

    // I am the message recipient
    var sender = event.getPubKey();
    log.debug("Decrypting message from {}", sender);
    MessageCipher cipher =
        new MessageCipher04(rcptId.getPrivateKey().getRawData(), sender.getRawData());
    return cipher.decrypt(event.getContent());
  }

  private static boolean amITheRecipient(@NonNull Identity recipient, @NonNull GenericEvent event) {
    // Use helper to fetch the p-tag without manual casts
    PubKeyTag pTag =
        Filterable.getTypeSpecificTags(PubKeyTag.class, event).stream()
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));

    if (Objects.equals(recipient.getPublicKey(), pTag.getPublicKey())) {
      return true;
    }

    if (Objects.equals(recipient.getPublicKey(), event.getPubKey())) {
      return false;
    }

    throw new RuntimeException("Unrelated event");
  }
}
