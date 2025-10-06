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

/**
 * NIP-44: Encrypted Payloads (Versioned Encrypted Messages).
 *
 * <p>This class provides utilities for encrypting and decrypting messages using NIP-44, which is
 * the <strong>recommended encryption standard</strong> for Nostr. NIP-44 uses XChaCha20-Poly1305
 * authenticated encryption (AEAD) with padding to prevent metadata leakage.
 *
 * <h2>What is NIP-44?</h2>
 *
 * <p>NIP-44 is the successor to NIP-04 and provides:
 * <ul>
 *   <li><strong>XChaCha20-Poly1305 AEAD:</strong> Authenticated encryption prevents tampering</li>
 *   <li><strong>Padding:</strong> Messages are padded to standard sizes to hide true length</li>
 *   <li><strong>Versioning:</strong> Version byte (0x02) allows future algorithm upgrades</li>
 *   <li><strong>HMAC-SHA256 for key derivation:</strong> Safer than raw ECDH</li>
 *   <li><strong>Protection against metadata leakage:</strong> Padding obscures message size</li>
 * </ul>
 *
 * <h2>NIP-44 vs NIP-04</h2>
 *
 * <table border="1">
 *   <tr>
 *     <th>Feature</th>
 *     <th>NIP-04 (Legacy)</th>
 *     <th>NIP-44 (Recommended)</th>
 *   </tr>
 *   <tr>
 *     <td><strong>Encryption</strong></td>
 *     <td>AES-256-CBC</td>
 *     <td>XChaCha20-Poly1305</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Authentication</strong></td>
 *     <td>None (vulnerable to tampering)</td>
 *     <td>AEAD (authenticated)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Padding</strong></td>
 *     <td>None (message length visible)</td>
 *     <td>Power-of-2 padding (hides length)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Key Derivation</strong></td>
 *     <td>Raw ECDH shared secret</td>
 *     <td>HMAC-SHA256(ECDH)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Versioning</strong></td>
 *     <td>No version byte</td>
 *     <td>Version byte (0x02)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Security</strong></td>
 *     <td>⚠️ Deprecated</td>
 *     <td>✅ Production-ready</td>
 *   </tr>
 * </table>
 *
 * <h2>When to Use NIP-44</h2>
 *
 * <p>Use NIP-44 for:
 * <ul>
 *   <li><strong>New applications:</strong> Always prefer NIP-44 over NIP-04</li>
 *   <li><strong>Private DMs:</strong> Kind 4 events with encrypted content</li>
 *   <li><strong>Encrypted content fields:</strong> Any event that needs encrypted data</li>
 *   <li><strong>Group messaging:</strong> When combined with multi-recipient protocols</li>
 * </ul>
 *
 * <p>Use NIP-04 only for:
 * <ul>
 *   <li>Backward compatibility with legacy clients</li>
 *   <li>Reading existing NIP-04 encrypted messages</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Encrypt a Message</h3>
 * <pre>{@code
 * Identity alice = new Identity("nsec1...");
 * PublicKey bob = new PublicKey("npub1...");
 *
 * String encrypted = NIP44.encrypt(alice, "Hello Bob!", bob);
 * // Returns a versioned encrypted payload (starts with 0x02)
 * }</pre>
 *
 * <h3>Example 2: Decrypt a Message</h3>
 * <pre>{@code
 * Identity bob = new Identity("nsec1...");
 * PublicKey alice = new PublicKey("npub1...");
 * String encrypted = "..."; // received encrypted message
 *
 * String plaintext = NIP44.decrypt(bob, encrypted, alice);
 * System.out.println(plaintext); // "Hello Bob!"
 * }</pre>
 *
 * <h3>Example 3: Decrypt an Encrypted DM Event (Kind 4)</h3>
 * <pre>{@code
 * Identity myIdentity = new Identity("nsec1...");
 * GenericEvent dmEvent = ... // received kind-4 event with NIP-44 encryption
 *
 * String plaintext = NIP44.decrypt(myIdentity, dmEvent);
 * // Works whether you're the sender or recipient
 * }</pre>
 *
 * <h3>Example 4: Create and Send an Encrypted DM (Manual)</h3>
 * <pre>{@code
 * Identity sender = new Identity("nsec1...");
 * PublicKey recipient = new PublicKey("npub1...");
 *
 * // Encrypt the message
 * String encrypted = NIP44.encrypt(sender, "Secret message", recipient);
 *
 * // Create a kind-4 event
 * GenericEvent dm = new GenericEvent(sender.getPublicKey(), Kind.ENCRYPTED_DIRECT_MESSAGE);
 * dm.setContent(encrypted);
 * dm.addTag(new PubKeyTag(recipient));
 *
 * // Sign and send
 * sender.sign(dm);
 * client.send(dm, relays);
 * }</pre>
 *
 * <h2>Encryption Format</h2>
 *
 * <p>NIP-44 ciphertext structure (base64-encoded):
 * <pre>
 * [version (1 byte)][nonce (32 bytes)][ciphertext (variable)][MAC (16 bytes)]
 * </pre>
 *
 * <ul>
 *   <li><strong>Version:</strong> 0x02 (current version)</li>
 *   <li><strong>Nonce:</strong> 32-byte random value (XChaCha20 nonce)</li>
 *   <li><strong>Ciphertext:</strong> Encrypted + padded message</li>
 *   <li><strong>MAC:</strong> 16-byte Poly1305 authentication tag</li>
 * </ul>
 *
 * <h2>Padding Scheme</h2>
 *
 * <p>Messages are padded to the next power-of-2 size (up to 64KB), hiding the true message length:
 * <ul>
 *   <li>0-32 bytes → padded to 32 bytes</li>
 *   <li>33-64 bytes → padded to 64 bytes</li>
 *   <li>65-128 bytes → padded to 128 bytes</li>
 *   <li>... and so on up to 65536 bytes</li>
 * </ul>
 *
 * <h2>Security Properties</h2>
 *
 * <ul>
 *   <li><strong>Confidentiality:</strong> XChaCha20 encryption</li>
 *   <li><strong>Authenticity:</strong> Poly1305 MAC prevents tampering</li>
 *   <li><strong>Forward secrecy:</strong> No (static key pairs)</li>
 *   <li><strong>Metadata protection:</strong> Padding hides message length</li>
 *   <li><strong>Replay protection:</strong> No (application-level responsibility)</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>All static methods in this class are thread-safe.
 *
 * <h2>Design Pattern</h2>
 *
 * <p>This class follows the <strong>Utility Pattern</strong>, providing static helper methods for:
 * <ul>
 *   <li>Message encryption (delegates to {@link MessageCipher44})</li>
 *   <li>Message decryption (delegates to {@link MessageCipher44})</li>
 *   <li>Event-based decryption (extracts keys from event tags)</li>
 * </ul>
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/44.md">NIP-44 Specification</a>
 * @see NIP04
 * @see nostr.encryption.MessageCipher44
 * @since 0.5.0
 */
@Slf4j
public class NIP44 extends EventNostr {

  /**
   * Encrypt a plaintext message using NIP-44 encryption (XChaCha20-Poly1305 AEAD).
   *
   * <p>This method performs NIP-44 encryption:
   * <ol>
   *   <li>Derives a shared secret using ECDH (sender's private key + recipient's public key)</li>
   *   <li>Derives an encryption key using HMAC-SHA256</li>
   *   <li>Pads the message to the next power-of-2 size (32, 64, 128, ..., 65536 bytes)</li>
   *   <li>Generates a random 32-byte nonce</li>
   *   <li>Encrypts with XChaCha20-Poly1305 AEAD</li>
   *   <li>Returns: base64([version][nonce][ciphertext][MAC])</li>
   * </ol>
   *
   * <p><strong>Security:</strong> This method provides both confidentiality (encryption) and
   * authenticity (MAC). Tampering with the ciphertext will be detected during decryption.
   *
   * <p><strong>Example:</strong>
   * <pre>{@code
   * Identity alice = new Identity("nsec1...");
   * PublicKey bob = new PublicKey("npub1...");
   *
   * String encrypted = NIP44.encrypt(alice, "Hello Bob!", bob);
   * // Returns base64-encoded versioned encrypted payload
   * }</pre>
   *
   * @param sender the identity of the sender (must contain private key for ECDH)
   * @param message the plaintext message to encrypt
   * @param recipient the recipient's public key
   * @return the encrypted message in NIP-44 format (base64-encoded)
   */
  public static String encrypt(
      @NonNull Identity sender, @NonNull String message, @NonNull PublicKey recipient) {
    MessageCipher cipher =
        new MessageCipher44(sender.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.encrypt(message);
  }

  /**
   * Decrypt a NIP-44 encrypted message using XChaCha20-Poly1305 AEAD.
   *
   * <p>This method performs NIP-44 decryption:
   * <ol>
   *   <li>Derives the same shared secret using ECDH</li>
   *   <li>Derives the decryption key using HMAC-SHA256</li>
   *   <li>Parses the encrypted format: [version][nonce][ciphertext][MAC]</li>
   *   <li>Verifies the Poly1305 MAC (throws if tampered)</li>
   *   <li>Decrypts using XChaCha20</li>
   *   <li>Removes padding and returns the plaintext</li>
   * </ol>
   *
   * <p>Either party (sender or recipient) can decrypt the message by providing their own private
   * key and the other party's public key.
   *
   * <p><strong>Security:</strong> If the MAC verification fails (message was tampered with),
   * decryption will fail with an exception.
   *
   * <p><strong>Example:</strong>
   * <pre>{@code
   * Identity bob = new Identity("nsec1...");
   * PublicKey alice = new PublicKey("npub1...");
   * String encrypted = "..."; // received NIP-44 encrypted message
   *
   * String plaintext = NIP44.decrypt(bob, encrypted, alice);
   * System.out.println(plaintext); // "Hello Bob!"
   * }</pre>
   *
   * @param identity the identity performing decryption (sender or recipient)
   * @param encrypteEPessage the encrypted message in NIP-44 format (base64-encoded)
   * @param recipient the public key of the other party (counterparty)
   * @return the decrypted plaintext message
   * @throws RuntimeException if MAC verification fails or decryption fails
   */
  public static String decrypt(
      @NonNull Identity identity, @NonNull String encrypteEPessage, @NonNull PublicKey recipient) {
    MessageCipher cipher =
        new MessageCipher44(identity.getPrivateKey().getRawData(), recipient.getRawData());
    return cipher.decrypt(encrypteEPessage);
  }

  /**
   * Decrypt a NIP-44 encrypted direct message event (kind 4 or other encrypted events).
   *
   * <p>This method automatically determines whether the provided identity is the sender or recipient
   * of the message, extracts the counterparty's public key from the event, and decrypts accordingly.
   *
   * <p>The method:
   * <ol>
   *   <li>Extracts the 'p' tag to identify the recipient/counterparty</li>
   *   <li>Determines if the identity is the sender or recipient</li>
   *   <li>Uses the appropriate keys for ECDH decryption</li>
   *   <li>Verifies the Poly1305 MAC</li>
   *   <li>Returns the plaintext content</li>
   * </ol>
   *
   * <p><strong>Example (as recipient):</strong>
   * <pre>{@code
   * Identity myIdentity = new Identity("nsec1...");
   * GenericEvent dmEvent = ... // received from relay (kind 4 with NIP-44 encryption)
   *
   * String message = NIP44.decrypt(myIdentity, dmEvent);
   * System.out.println("Received: " + message);
   * }</pre>
   *
   * <p><strong>Example (as sender, reading your own DM):</strong>
   * <pre>{@code
   * Identity myIdentity = new Identity("nsec1...");
   * GenericEvent myDmEvent = ... // a DM I sent with NIP-44
   *
   * String message = NIP44.decrypt(myIdentity, myDmEvent);
   * System.out.println("I sent: " + message);
   * }</pre>
   *
   * @param recipient the identity attempting to decrypt (must be either sender or recipient)
   * @param event the encrypted event (typically kind 4, but can be any event with encrypted content)
   * @return the decrypted plaintext content
   * @throws NoSuchElementException if no 'p' tag is found in the event
   * @throws RuntimeException if the identity is neither the sender nor the recipient, or if MAC verification fails
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
