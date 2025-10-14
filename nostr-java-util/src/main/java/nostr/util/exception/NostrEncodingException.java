package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when encoding or decoding Nostr data fails (JSON, Bech32, hex, base64).
 *
 * <p>This exception indicates that data could not be serialized to or deserialized from a wire
 * format. It's commonly thrown during JSON encoding/decoding, Bech32 encoding/decoding, or hex/base64
 * conversions.
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li><strong>Invalid JSON:</strong> Malformed JSON event, message, or tag structure</li>
 *   <li><strong>Bech32 decode failed:</strong> Invalid npub/nsec/note format or checksum error</li>
 *   <li><strong>Hex conversion failed:</strong> Invalid hexadecimal string (odd length, invalid chars)</li>
 *   <li><strong>Base64 decode failed:</strong> Invalid base64 string (NIP-04/NIP-44 encrypted content)</li>
 *   <li><strong>Serialization failed:</strong> Object couldn't be converted to JSON</li>
 *   <li><strong>Missing fields:</strong> Required JSON fields are absent during deserialization</li>
 *   <li><strong>Type mismatch:</strong> JSON field has unexpected type (string instead of number, etc.)</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Handling JSON Parsing Errors</h3>
 * <pre>{@code
 * try {
 *     String eventJson = "{\"id\":\"bad json\"}"; // missing closing quote
 *     GenericEvent event = GenericEvent.fromJson(eventJson);
 * } catch (NostrEncodingException e) {
 *     logger.error("Failed to parse event JSON: {}", e.getMessage());
 *     // Ignore malformed events from relay
 * }
 * }</pre>
 *
 * <h3>Example 2: Handling Bech32 Decoding Errors</h3>
 * <pre>{@code
 * try {
 *     String invalidNpub = "npub1invalidchecksum";
 *     PublicKey pubKey = new PublicKey(Bech32.fromBech32(invalidNpub));
 * } catch (NostrEncodingException e) {
 *     logger.error("Invalid npub format: {}", e.getMessage());
 *     // Show error to user
 * } catch (Exception e) {
 *     logger.error("Bech32 decoding failed: {}", e.getMessage());
 * }
 * }</pre>
 *
 * <h3>Example 3: Handling Hex Conversion Errors</h3>
 * <pre>{@code
 * try {
 *     String hexKey = "not_valid_hex_123";
 *     PublicKey pubKey = new PublicKey(hexKey);
 * } catch (NostrEncodingException e) {
 *     logger.error("Invalid hex key: {}", e.getMessage());
 *     // Public key must be 64-char hex string
 * }
 * }</pre>
 *
 * <h3>Example 4: Handling Event Serialization Errors</h3>
 * <pre>{@code
 * try {
 *     GenericEvent event = ... // event with circular references or other issues
 *     String json = event.toJson();
 * } catch (NostrEncodingException e) {
 *     logger.error("Failed to serialize event: {}", e.getMessage(), e);
 *     // Fix the event structure
 * }
 * }</pre>
 *
 * <h2>Recovery Strategies</h2>
 *
 * <ul>
 *   <li><strong>JSON parsing:</strong> Validate JSON structure, log and ignore malformed messages</li>
 *   <li><strong>Bech32 decoding:</strong> Validate format (npub/nsec/note prefix), show user-friendly errors</li>
 *   <li><strong>Hex conversion:</strong> Validate length (64 chars for keys, 32 bytes) and charset</li>
 *   <li><strong>User input:</strong> Provide clear validation messages (\"Invalid public key format\")</li>
 * </ul>
 *
 * <h2>Encoding Formats in Nostr</h2>
 *
 * <table border="1">
 *   <tr>
 *     <th>Format</th>
 *     <th>Usage</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td><strong>JSON</strong></td>
 *     <td>Events, messages, tags</td>
 *     <td>{\"id\":\"...\",\"kind\":1,...}</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Bech32</strong></td>
 *     <td>Public keys (npub), private keys (nsec), event IDs (note)</td>
 *     <td>npub180cvv07tjdrrgpa0j...</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Hex</strong></td>
 *     <td>Keys, event IDs, signatures</td>
 *     <td>3bf0c63fcb93463407af...</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Base64</strong></td>
 *     <td>Encrypted content (NIP-04/NIP-44)</td>
 *     <td>SGVsbG8gV29ybGQh</td>
 *   </tr>
 * </table>
 *
 * @see nostr.event.json.codec.BaseEventEncoder
 * @see nostr.crypto.bech32.Bech32
 * @see nostr.util.NostrUtil
 * @since 0.1.0
 */
@StandardException
public class NostrEncodingException extends NostrRuntimeException {}
