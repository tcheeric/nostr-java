package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Base unchecked exception for all Nostr-related errors in the SDK.
 *
 * <p>This is the root of the exception hierarchy for nostr-java. All exceptions thrown by this
 * SDK extend from {@code NostrRuntimeException}, making it easy to catch all Nostr-specific errors
 * with a single catch block.
 *
 * <h2>Exception Hierarchy</h2>
 *
 * <pre>
 * NostrRuntimeException (base)
 *  ├── NostrProtocolException (protocol violations, invalid events/messages)
 *  │    └── NostrException (legacy, deprecated)
 *  ├── NostrCryptoException (signing, verification, key generation failures)
 *  ├── NostrEncodingException (JSON/Bech32/hex encoding/decoding failures)
 *  └── NostrNetworkException (relay connection, WebSocket, timeout failures)
 * </pre>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><strong>Unchecked exceptions:</strong> All exceptions extend {@link RuntimeException} (no forced try-catch)</li>
 *   <li><strong>Domain-specific:</strong> Each subclass represents a specific failure domain</li>
 *   <li><strong>Fail fast:</strong> Validation errors are thrown immediately (not silently ignored)</li>
 *   <li><strong>Context-rich messages:</strong> Exceptions include detailed context for debugging</li>
 * </ul>
 *
 * <h2>When to Use</h2>
 *
 * <p>You should catch {@code NostrRuntimeException} when:
 * <ul>
 *   <li>You want to catch all Nostr-related errors</li>
 *   <li>You need to distinguish Nostr errors from other exceptions</li>
 *   <li>You're implementing error boundaries in your application</li>
 * </ul>
 *
 * <p>Prefer catching specific subclasses when:
 * <ul>
 *   <li>You can handle specific error types differently</li>
 *   <li>You want to retry on network errors but fail on protocol errors</li>
 *   <li>You need fine-grained error handling</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Catch All Nostr Errors</h3>
 * <pre>{@code
 * try {
 *     GenericEvent event = ...;
 *     event.validate();
 *     event.sign(identity);
 *     client.send(event, relays);
 * } catch (NostrRuntimeException e) {
 *     logger.error("Nostr operation failed: {}", e.getMessage(), e);
 *     // Handle any Nostr-related error
 * }
 * }</pre>
 *
 * <h3>Example 2: Catch Specific Error Types</h3>
 * <pre>{@code
 * try {
 *     event.validate();
 * } catch (NostrProtocolException e) {
 *     // Invalid event data (bad kind, missing fields, etc.)
 *     logger.error("Protocol violation: {}", e.getMessage());
 * } catch (NostrCryptoException e) {
 *     // Signing or verification failed
 *     logger.error("Crypto error: {}", e.getMessage());
 * }
 * }</pre>
 *
 * <h3>Example 3: Retry on Network Errors</h3>
 * <pre>{@code
 * int maxRetries = 3;
 * for (int i = 0; i < maxRetries; i++) {
 *     try {
 *         client.send(event, relays);
 *         break; // success
 *     } catch (NostrNetworkException e) {
 *         if (i == maxRetries - 1) throw e;
 *         logger.warn("Network error, retrying... ({}/{})", i + 1, maxRetries);
 *         Thread.sleep(1000 * (i + 1)); // exponential backoff
 *     }
 * }
 * }</pre>
 *
 * <h2>Subclass Responsibilities</h2>
 *
 * <table border="1">
 *   <tr>
 *     <th>Exception Type</th>
 *     <th>When Thrown</th>
 *     <th>Examples</th>
 *   </tr>
 *   <tr>
 *     <td>{@link NostrProtocolException}</td>
 *     <td>NIP violations, invalid events</td>
 *     <td>Invalid kind, missing required tags, bad event structure</td>
 *   </tr>
 *   <tr>
 *     <td>{@link NostrCryptoException}</td>
 *     <td>Cryptographic failures</td>
 *     <td>Schnorr signing failed, invalid signature, key derivation error</td>
 *   </tr>
 *   <tr>
 *     <td>{@link NostrEncodingException}</td>
 *     <td>Serialization/deserialization errors</td>
 *     <td>Invalid JSON, Bech32 decode failed, hex conversion error</td>
 *   </tr>
 *   <tr>
 *     <td>{@link NostrNetworkException}</td>
 *     <td>Network/relay communication errors</td>
 *     <td>Connection timeout, WebSocket closed, relay rejected event</td>
 *   </tr>
 * </table>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li><strong>Use specific exceptions:</strong> Throw the most specific subclass that applies</li>
 *   <li><strong>Include context:</strong> Exception messages should describe what failed and why</li>
 *   <li><strong>Chain exceptions:</strong> Use {@code new NostrException("msg", cause)} to preserve stack traces</li>
 *   <li><strong>Document throws:</strong> Use {@code @throws} in JavaDoc to document expected exceptions</li>
 * </ul>
 *
 * @see NostrProtocolException
 * @see NostrCryptoException
 * @see NostrEncodingException
 * @see NostrNetworkException
 * @since 0.1.0
 */
@StandardException
public class NostrRuntimeException extends RuntimeException {}
