package nostr.event.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.json.EventJsonMapper;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

/**
 * Serializes Nostr events according to NIP-01 canonical format.
 *
 * <p>This class provides methods for converting event fields into the canonical JSON
 * serialization required by NIP-01. The serialization is deterministic and produces
 * the same output for the same inputs, which is critical for event ID computation
 * and signature verification.
 *
 * <p><b>Canonical Format:</b> Events are serialized as JSON arrays with specific ordering:
 * <pre>{@code
 * [
 *   0,                              // Protocol version (always 0)
 *   "pubkey_hex_string",           // 64-char public key hex
 *   1234567890,                     // Unix timestamp (created_at)
 *   1,                              // Event kind integer
 *   [["e","event_id"],["p","pk"]], // Tags as array of arrays
 *   "Event content string"          // Content (can be empty string)
 * ]
 * }</pre>
 *
 * <p><b>Usage:</b> This serialization format is used for:
 * <ul>
 *   <li><b>Event ID Computation:</b> SHA-256 hash of the serialized event</li>
 *   <li><b>Signature Creation:</b> BIP-340 Schnorr signature of the event ID</li>
 *   <li><b>Signature Verification:</b> Relays recompute ID and verify signature</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * // Serialize event fields
 * String json = EventSerializer.serialize(
 *     publicKey,
 *     Instant.now().getEpochSecond(),
 *     Kind.TEXT_NOTE.getValue(),
 *     List.of(new HashtagTag("nostr")),
 *     "Hello Nostr!"
 * );
 *
 * // Compute event ID from serialization
 * byte[] bytes = EventSerializer.serializeToBytes(...);
 * String eventId = EventSerializer.computeEventId(bytes);
 *
 * // Or do both in one call
 * String eventId = EventSerializer.serializeAndComputeId(...);
 * }</pre>
 *
 * <p><b>Design:</b> This class uses the Utility Pattern with static methods. It uses
 * {@link EventJsonMapper} for consistent JSON configuration across the library.
 *
 * <p><b>Thread Safety:</b> All methods are stateless and thread-safe.
 *
 * <p><b>Determinism:</b> The serialization is deterministic - same inputs always produce
 * the same output. This is essential for:
 * <ul>
 *   <li>Event ID verification by relays</li>
 *   <li>Signature verification by other clients</li>
 *   <li>Duplicate event detection</li>
 * </ul>
 *
 * @see EventJsonMapper
 * @see nostr.event.impl.GenericEvent#update()
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
 * @since 0.6.2
 */
public final class EventSerializer {

  private static final ObjectMapper MAPPER = EventJsonMapper.getMapper();

  private EventSerializer() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Serializes event fields into NIP-01 canonical JSON format.
   *
   * <p>The serialized format is deterministic and used for computing event IDs and signatures.
   *
   * @param pubKey public key of event creator
   * @param createdAt Unix timestamp when event was created
   * @param kind event kind integer
   * @param tags event tags
   * @param content event content
   * @return canonical JSON string representation
   * @throws NostrException if serialization fails
   */
  public static String serialize(
      @NonNull PublicKey pubKey,
      @NonNull Long createdAt,
      @NonNull Integer kind,
      @NonNull List<BaseTag> tags,
      @NonNull String content)
      throws NostrException {

    var arrayNode = JsonNodeFactory.instance.arrayNode();

    try {
      arrayNode.add(0); // Protocol version
      arrayNode.add(pubKey.toString());
      arrayNode.add(createdAt);
      arrayNode.add(kind);
      arrayNode.add(MAPPER.valueToTree(tags));
      arrayNode.add(content);

      return MAPPER.writeValueAsString(arrayNode);
    } catch (JsonProcessingException e) {
      throw new NostrException("Failed to serialize event: " + e.getMessage(), e);
    }
  }

  /**
   * Serializes event and converts to UTF-8 bytes.
   *
   * @param pubKey public key of event creator
   * @param createdAt Unix timestamp when event was created
   * @param kind event kind integer
   * @param tags event tags
   * @param content event content
   * @return UTF-8 encoded bytes of serialized event
   * @throws NostrException if serialization fails
   */
  public static byte[] serializeToBytes(
      @NonNull PublicKey pubKey,
      @NonNull Long createdAt,
      @NonNull Integer kind,
      @NonNull List<BaseTag> tags,
      @NonNull String content)
      throws NostrException {

    return serialize(pubKey, createdAt, kind, tags, content).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Computes event ID from serialized event.
   *
   * <p>The event ID is the SHA-256 hash of the serialized event, represented as a 64-character
   * lowercase hex string.
   *
   * @param serializedEvent UTF-8 bytes of serialized event
   * @return event ID as 64-character hex string
   * @throws NostrException if hashing fails
   */
  public static String computeEventId(byte[] serializedEvent) throws NostrException {
    try {
      return NostrUtil.bytesToHex(NostrUtil.sha256(serializedEvent));
    } catch (NoSuchAlgorithmException e) {
      throw new NostrException("SHA-256 algorithm not available", e);
    }
  }

  /**
   * Serializes event and computes event ID in one operation.
   *
   * @param pubKey public key of event creator
   * @param createdAt Unix timestamp when event was created (if null, uses current time)
   * @param kind event kind integer
   * @param tags event tags
   * @param content event content
   * @return computed event ID as 64-character hex string
   * @throws NostrException if serialization or hashing fails
   */
  public static String serializeAndComputeId(
      @NonNull PublicKey pubKey,
      Long createdAt,
      @NonNull Integer kind,
      @NonNull List<BaseTag> tags,
      @NonNull String content)
      throws NostrException {

    Long timestamp = createdAt != null ? createdAt : Instant.now().getEpochSecond();
    byte[] serialized = serializeToBytes(pubKey, timestamp, kind, tags, content);
    return computeEventId(serialized);
  }
}
