package nostr.crypto.bech32;

import lombok.Getter;

/**
 * NIP-19: Bech32-encoded entity prefixes for Nostr.
 *
 * <p>This enum defines the Human-Readable Prefixes (HRPs) used in NIP-19 for encoding various
 * Nostr entities using Bech32 format. NIP-19 specifies standardized prefixes to make Nostr
 * identifiers and references human-readable and type-safe.
 *
 * <h2>What is NIP-19?</h2>
 *
 * <p>NIP-19 defines Bech32-encoded entities for:
 * <ul>
 *   <li><strong>npub:</strong> Public keys (32 bytes hex → npub1...)</li>
 *   <li><strong>nsec:</strong> Private keys (32 bytes hex → nsec1...)</li>
 *   <li><strong>note:</strong> Event IDs (32 bytes hex → note1...)</li>
 *   <li><strong>nprofile:</strong> Profile with metadata (public key + relays)</li>
 *   <li><strong>nevent:</strong> Event reference with metadata (event ID + relays + author)</li>
 * </ul>
 *
 * <h2>Why Bech32?</h2>
 *
 * <p>Bech32 encoding provides:
 * <ul>
 *   <li><strong>Type safety:</strong> Prefix indicates what the string represents</li>
 *   <li><strong>Error detection:</strong> Built-in checksum catches typos</li>
 *   <li><strong>Human-friendly:</strong> Case-insensitive, no ambiguous characters (0/O, 1/I/l)</li>
 *   <li><strong>Copy-paste safety:</strong> No special characters that break in URLs</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Encode a Public Key</h3>
 * <pre>{@code
 * String hexPubKey = "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d";
 * String npub = Bech32.toBech32(Bech32Prefix.NPUB, hexPubKey);
 * // Returns: "npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"
 * }</pre>
 *
 * <h3>Example 2: Encode a Private Key</h3>
 * <pre>{@code
 * byte[] privateKeyBytes = ... // 32 bytes
 * String nsec = Bech32.toBech32(Bech32Prefix.NSEC, privateKeyBytes);
 * // Returns: "nsec1..."
 * }</pre>
 *
 * <h3>Example 3: Encode an Event ID</h3>
 * <pre>{@code
 * String eventId = "a1b2c3d4..."; // 32-byte hex
 * String note = Bech32.toBech32(Bech32Prefix.NOTE, eventId);
 * // Returns: "note1..."
 * }</pre>
 *
 * <h3>Example 4: Using with PublicKey/PrivateKey</h3>
 * <pre>{@code
 * PublicKey pubKey = new PublicKey("3bf0c63fcb93...");
 * String bech32 = pubKey.toBech32(); // Uses NPUB prefix automatically
 *
 * PrivateKey privKey = new PrivateKey("secret_hex...");
 * String nsec = privKey.toBech32(); // Uses NSEC prefix automatically
 * }</pre>
 *
 * <h2>Supported Prefixes</h2>
 *
 * <table border="1">
 *   <tr>
 *     <th>Prefix</th>
 *     <th>Type</th>
 *     <th>Description</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td><strong>npub</strong></td>
 *     <td>Public Key</td>
 *     <td>32-byte public key (hex)</td>
 *     <td>npub180cvv07tjdrrgpa0j...</td>
 *   </tr>
 *   <tr>
 *     <td><strong>nsec</strong></td>
 *     <td>Private Key</td>
 *     <td>32-byte private key (hex)</td>
 *     <td>nsec1vl029mgpspedva04g...</td>
 *   </tr>
 *   <tr>
 *     <td><strong>note</strong></td>
 *     <td>Event ID</td>
 *     <td>32-byte event ID (hex)</td>
 *     <td>note1fntxtkcy9pjwuc...</td>
 *   </tr>
 *   <tr>
 *     <td><strong>nprofile</strong></td>
 *     <td>Profile</td>
 *     <td>Public key + relay hints + metadata</td>
 *     <td>nprofile1qqsrhuxx8...</td>
 *   </tr>
 *   <tr>
 *     <td><strong>nevent</strong></td>
 *     <td>Event Reference</td>
 *     <td>Event ID + relay hints + author</td>
 *     <td>nevent1qqstna2yrezu...</td>
 *   </tr>
 * </table>
 *
 * <h2>Security Considerations</h2>
 *
 * <ul>
 *   <li><strong>NEVER share nsec:</strong> Private keys must be kept secret</li>
 *   <li><strong>Validate prefixes:</strong> Check the prefix matches the expected type before using</li>
 *   <li><strong>Checksum validation:</strong> Always validate the Bech32 checksum when decoding</li>
 * </ul>
 *
 * <h2>Implementation Notes</h2>
 *
 * <p>This implementation uses:
 * <ul>
 *   <li><strong>BIP-173:</strong> Original Bech32 spec (for npub, nsec, note)</li>
 *   <li><strong>BIP-350:</strong> Bech32m variant (for nprofile, nevent with TLV encoding)</li>
 * </ul>
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/19.md">NIP-19 Specification</a>
 * @see Bech32
 * @see nostr.base.PublicKey#toBech32()
 * @see nostr.base.PrivateKey#toBech32()
 * @author squirrel
 * @since 0.1.0
 */
@Getter
public enum Bech32Prefix {
  NPUB("npub", "public keys"),
  NSEC("nsec", "private keys"),
  NOTE("note", "note ids"),
  NPROFILE("nprofile", "nostr profile"),
  NEVENT("nevent", "nostr event");

  private final String code;
  private final String description;

  Bech32Prefix(String code, String description) {
    this.code = code;
    this.description = description;
  }
}
