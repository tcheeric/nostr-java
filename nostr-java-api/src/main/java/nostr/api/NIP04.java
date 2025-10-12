package nostr.api;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.encryption.MessageCipher;
import nostr.encryption.MessageCipher04;
import nostr.event.BaseTag;
import nostr.event.filter.Filterable;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * NIP-04: Encrypted Direct Messages.
 *
 * <p>This class provides utilities for creating, encrypting, and decrypting private direct messages
 * (DMs) on the Nostr protocol. NIP-04 uses AES-256-CBC encryption with a shared secret derived from
 * ECDH (Elliptic Curve Diffie-Hellman) key agreement.
 *
 * <h2>What is NIP-04?</h2>
 *
 * <p>NIP-04 defines encrypted direct messages as kind-4 events where:
 * <ul>
 *   <li>The content is encrypted using AES-256-CBC</li>
 *   <li>The encryption key is derived from ECDH between sender and recipient</li>
 *   <li>A 'p' tag indicates the recipient's public key</li>
 *   <li>The encrypted content format is: base64(ciphertext)?iv=base64(initialization_vector)</li>
 * </ul>
 *
 * <h2>Security Note</h2>
 *
 * <p><strong>NIP-04 is deprecated for new applications.</strong> Use NIP-44 instead, which provides:
 * <ul>
 *   <li>Better encryption scheme (XChaCha20-Poly1305)</li>
 *   <li>Authenticated encryption (AEAD)</li>
 *   <li>Protection against padding oracle attacks</li>
 *   <li>No metadata leakage through message length</li>
 * </ul>
 *
 * <p>NIP-04 is maintained for backward compatibility with existing clients and messages.
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Send an Encrypted DM</h3>
 * <pre>{@code
 * Identity sender = new Identity("nsec1...");
 * PublicKey recipient = new PublicKey("npub1...");
 *
 * NIP04 nip04 = new NIP04(sender, recipient);
 * nip04.createDirectMessageEvent("Hello! This is a private message.")
 *      .sign()
 *      .send(relays);
 * }</pre>
 *
 * <h3>Example 2: Decrypt a Received DM (as recipient)</h3>
 * <pre>{@code
 * Identity myIdentity = new Identity("nsec1...");
 * GenericEvent dmEvent = ... // received from relay (kind 4)
 *
 * String plaintext = NIP04.decrypt(myIdentity, dmEvent);
 * System.out.println("Received: " + plaintext);
 * }</pre>
 *
 * <h3>Example 3: Decrypt Your Own Sent DM</h3>
 * <pre>{@code
 * Identity myIdentity = new Identity("nsec1...");
 * GenericEvent myDmEvent = ... // a DM I sent (kind 4)
 *
 * // Works for both sender and recipient
 * String plaintext = NIP04.decrypt(myIdentity, myDmEvent);
 * System.out.println("I sent: " + plaintext);
 * }</pre>
 *
 * <h3>Example 4: Standalone Encrypt/Decrypt</h3>
 * <pre>{@code
 * Identity sender = new Identity("nsec1...");
 * PublicKey recipient = new PublicKey("npub1...");
 *
 * // Encrypt a message
 * String encrypted = NIP04.encrypt(sender, "Secret message", recipient);
 *
 * // Decrypt it (either party can decrypt with their private key + other's public key)
 * String decrypted = NIP04.decrypt(sender, encrypted, recipient);
 * }</pre>
 *
 * <h2>Design Pattern</h2>
 *
 * <p>This class follows the <strong>Facade Pattern</strong>, providing a simplified interface for:
 * <ul>
 *   <li>Event creation (delegates to {@code GenericEventFactory})</li>
 *   <li>Encryption (delegates to {@code MessageCipher04})</li>
 *   <li>Decryption (delegates to {@code MessageCipher04})</li>
 *   <li>Event signing and sending (inherited from {@code EventNostr})</li>
 * </ul>
 *
 * <h2>How Encryption Works</h2>
 *
 * <ol>
 *   <li><strong>Key Agreement:</strong> ECDH produces a shared secret from sender's private key + recipient's public key</li>
 *   <li><strong>IV Generation:</strong> A random 16-byte initialization vector is generated</li>
 *   <li><strong>Encryption:</strong> AES-256-CBC encrypts the plaintext message</li>
 *   <li><strong>Format:</strong> Output is base64(ciphertext)?iv=base64(iv)</li>
 * </ol>
 *
 * <h2>Known Limitations</h2>
 *
 * <ul>
 *   <li><strong>No authentication:</strong> Vulnerable to tampering (use NIP-44 for AEAD)</li>
 *   <li><strong>Padding oracle risk:</strong> CBC mode can leak info through padding errors</li>
 *   <li><strong>Metadata leakage:</strong> Message length is visible (NIP-44 pads to fixed sizes)</li>
 *   <li><strong>Replay attacks:</strong> No nonce/counter mechanism</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>This class is <strong>not thread-safe</strong> for instance methods. Each thread should create
 * its own {@code NIP04} instance. The static {@code encrypt()} and {@code decrypt()} methods are
 * thread-safe.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/04.md">NIP-04 Specification</a>
 * @see NIP44
 * @see nostr.encryption.MessageCipher04
 * @since 0.1.0
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
   * Create a NIP-04 encrypted direct message event (kind 4).
   *
   * <p>This method:
   * <ol>
   *   <li>Encrypts the plaintext content using AES-256-CBC</li>
   *   <li>Adds a 'p' tag with the recipient's public key</li>
   *   <li>Creates a kind-4 event with the encrypted content</li>
   *   <li>Stores the event in this instance for signing/sending</li>
   * </ol>
   *
   * <p>The event is NOT signed or sent automatically. Chain with {@code .sign()} and
   * {@code .send(relays)} to complete the operation.
   *
   * <p><strong>Example:</strong>
   * <pre>{@code
   * NIP04 nip04 = new NIP04(senderIdentity, recipientPubKey);
   * nip04.createDirectMessageEvent("Hello, this is private!")
   *      .sign()
   *      .send(relays);
   * }</pre>
   *
   * @param content the plaintext message to encrypt and send
   * @return this instance for method chaining
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP04 createDirectMessageEvent(@NonNull String content) {
    log.debug("Creating direct message event");
    var encryptedContent = encrypt(getSender(), content, getRecipient());
    List<BaseTag> tags = List.of(new PubKeyTag(getRecipient()));

    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.ENCRYPTED_DIRECT_MESSAGE.getValue(), tags, encryptedContent)
            .create();
    this.updateEvent(genericEvent);

    return this;
  }

  /**
   * Encrypt the content of the current event (must be a kind-4 event).
   *
   * <p>This method encrypts the plaintext content stored in the current event using NIP-04
   * encryption. It extracts the recipient from the 'p' tag and uses AES-256-CBC encryption.
   *
   * <p><strong>Note:</strong> This is only needed if you manually created an event. The
   * {@link #createDirectMessageEvent(String)} method already encrypts the content automatically.
   *
   * @return this instance for method chaining
   * @throws IllegalArgumentException if the event is not kind 4
   * @throws NoSuchElementException if no 'p' tag is found in the event
   */
  public NIP04 encrypt() {
    encryptDirectMessage(getSender(), getEvent());
    return this;
  }

  /**
   * Encrypt a plaintext message using NIP-04 encryption (AES-256-CBC + ECDH).
   *
   * <p>This is a standalone utility method for encrypting messages without creating a full event.
   * The encryption process:
   * <ol>
   *   <li>Derives a shared secret using ECDH (sender's private key + recipient's public key)</li>
   *   <li>Generates a random 16-byte initialization vector (IV)</li>
   *   <li>Encrypts the message using AES-256-CBC</li>
   *   <li>Returns: base64(ciphertext)?iv=base64(iv)</li>
   * </ol>
   *
   * <p><strong>Example:</strong>
   * <pre>{@code
   * Identity alice = new Identity("nsec1...");
   * PublicKey bob = new PublicKey("npub1...");
   *
   * String encrypted = NIP04.encrypt(alice, "Hello Bob!", bob);
   * // Returns something like: "SGVsbG8gQm9iIQ==?iv=randomBase64IV=="
   * }</pre>
   *
   * @param senderId the sender's identity (contains private key for ECDH)
   * @param message the plaintext message to encrypt
   * @param recipient the recipient's public key
   * @return the encrypted message in NIP-04 format: base64(ciphertext)?iv=base64(iv)
   */
  public static String encrypt(
      @NonNull Identity senderId, @NonNull String message, @NonNull PublicKey recipient) {
    log.debug("Encrypting message from {} to {}", senderId.getPublicKey(), recipient);
    MessageCipher cipher =
        new MessageCipher04(senderId.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.encrypt(message);
  }

  /**
   * Decrypt an encrypted message using NIP-04 decryption (AES-256-CBC + ECDH).
   *
   * <p>This is a standalone utility method for decrypting NIP-04 encrypted messages. Either party
   * (sender or recipient) can decrypt the message by providing their own private key and the other
   * party's public key.
   *
   * <p>The decryption process:
   * <ol>
   *   <li>Parses the encrypted format: base64(ciphertext)?iv=base64(iv)</li>
   *   <li>Derives the same shared secret using ECDH</li>
   *   <li>Decrypts using AES-256-CBC with the extracted IV</li>
   *   <li>Returns the plaintext message</li>
   * </ol>
   *
   * <p><strong>Example:</strong>
   * <pre>{@code
   * Identity bob = new Identity("nsec1...");
   * PublicKey alice = new PublicKey("npub1...");
   *
   * String encrypted = "SGVsbG8gQm9iIQ==?iv=randomBase64IV==";
   * String plaintext = NIP04.decrypt(bob, encrypted, alice);
   * // Returns: "Hello Bob!"
   * }</pre>
   *
   * @param identity the identity of the party decrypting (sender or recipient)
   * @param encryptedMessage the encrypted message in NIP-04 format
   * @param recipient the public key of the other party (if you're the sender, this is the recipient; if you're the recipient, this is the sender)
   * @return the decrypted plaintext message
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

    if (directMessageEvent.getKind() != Kind.ENCRYPTED_DIRECT_MESSAGE.getValue()) {
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
   * Decrypt an encrypted direct message event (kind 4).
   *
   * <p>This method automatically determines whether the provided identity is the sender or recipient
   * of the message, and decrypts accordingly. Both parties can decrypt the same message.
   *
   * <p>The method:
   * <ol>
   *   <li>Validates the event is kind 4 (encrypted DM)</li>
   *   <li>Extracts the 'p' tag to identify the recipient</li>
   *   <li>Determines if the identity is the sender or recipient</li>
   *   <li>Uses the appropriate keys for ECDH decryption</li>
   *   <li>Returns the plaintext content</li>
   * </ol>
   *
   * <p><strong>Example (as recipient):</strong>
   * <pre>{@code
   * Identity myIdentity = new Identity("nsec1...");
   * GenericEvent dmEvent = ... // received from relay
   *
   * String message = NIP04.decrypt(myIdentity, dmEvent);
   * System.out.println("Received: " + message);
   * }</pre>
   *
   * <p><strong>Example (as sender, reading your own DM):</strong>
   * <pre>{@code
   * Identity myIdentity = new Identity("nsec1...");
   * GenericEvent myDmEvent = ... // a DM I sent
   *
   * String message = NIP04.decrypt(myIdentity, myDmEvent);
   * System.out.println("I sent: " + message);
   * }</pre>
   *
   * @param rcptId the identity attempting to decrypt (must be either sender or recipient)
   * @param event the encrypted direct message event (must be kind 4)
   * @return the decrypted plaintext message
   * @throws IllegalArgumentException if the event is not kind 4
   * @throws NoSuchElementException if no 'p' tag is found in the event
   * @throws RuntimeException if the identity is neither the sender nor the recipient
   */
  public static String decrypt(@NonNull Identity rcptId, @NonNull GenericEvent event) {

    if (event.getKind() != Kind.ENCRYPTED_DIRECT_MESSAGE.getValue()) {
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
