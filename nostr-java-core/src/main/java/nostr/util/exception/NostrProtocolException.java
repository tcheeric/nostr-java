package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when Nostr protocol violations or NIP specification inconsistencies are detected.
 *
 * <p>This exception indicates that an event, message, or operation violates the Nostr protocol
 * (NIP-01) or a specific NIP specification. It is thrown during validation, parsing, or when
 * constructing events that don't conform to the expected structure.
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li><strong>Invalid event structure:</strong> Missing required fields (id, pubkey, created_at, kind, tags, content, sig)</li>
 *   <li><strong>Invalid kind:</strong> Kind value outside valid ranges or unsupported</li>
 *   <li><strong>Missing required tags:</strong> NIP-specific required tags are absent (e.g., 'p' tag for DMs)</li>
 *   <li><strong>Invalid tag structure:</strong> Tags don't follow the expected format</li>
 *   <li><strong>Signature mismatch:</strong> Event signature doesn't match the computed hash</li>
 *   <li><strong>Invalid timestamp:</strong> created_at is in the future or unreasonably old</li>
 *   <li><strong>Content validation failed:</strong> Content doesn't match expected format for the kind</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Catching Validation Errors</h3>
 * <pre>{@code
 * try {
 *     GenericEvent event = new GenericEvent(pubKey, Kind.TEXT_NOTE);
 *     event.setContent(""); // empty content might be invalid for some NIPs
 *     event.validate(); // throws if invalid
 * } catch (NostrProtocolException e) {
 *     logger.error("Event validation failed: {}", e.getMessage());
 *     // Handle protocol violation
 * }
 * }</pre>
 *
 * <h3>Example 2: Handling Message Parsing Errors</h3>
 * <pre>{@code
 * try {
 *     String relayMessage = "[\"INVALID\", \"malformed\"]";
 *     BaseMessage message = BaseMessageDecoder.decode(relayMessage);
 * } catch (NostrProtocolException e) {
 *     logger.error("Invalid relay message: {}", e.getMessage());
 *     // Ignore or log malformed messages from relay
 * }
 * }</pre>
 *
 * <h3>Example 3: Ensuring NIP Compliance</h3>
 * <pre>{@code
 * GenericEvent dmEvent = new GenericEvent(pubKey, Kind.ENCRYPTED_DIRECT_MESSAGE);
 * dmEvent.setContent("encrypted content...");
 *
 * // DM events require a 'p' tag (NIP-04)
 * if (dmEvent.getTags().stream().noneMatch(t -> t.getCode().equals("p"))) {
 *     throw new NostrProtocolException("DM event missing required 'p' tag (NIP-04)");
 * }
 * }</pre>
 *
 * <h2>Recovery Strategies</h2>
 *
 * <ul>
 *   <li><strong>Validation failures:</strong> Fix the event data before retrying</li>
 *   <li><strong>Relay messages:</strong> Log and ignore malformed messages (don't crash)</li>
 *   <li><strong>User input:</strong> Show validation errors to the user for correction</li>
 *   <li><strong>Protocol changes:</strong> Update SDK to support new NIPs/versions</li>
 * </ul>
 *
 * @see nostr.event.impl.GenericEvent#validate()
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01 Specification</a>
 * @since 0.1.0
 */
@StandardException
public class NostrProtocolException extends NostrRuntimeException {}
