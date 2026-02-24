package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when network communication with relays or external services fails.
 *
 * <p>This exception indicates that network-level operations failed, such as connecting to a relay,
 * sending events, receiving messages, or timeouts. It's typically thrown by WebSocket client
 * implementations and relay communication code.
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li><strong>Connection failed:</strong> Relay is unreachable or refuses connection</li>
 *   <li><strong>Timeout:</strong> Operation exceeded configured timeout (default 60 seconds)</li>
 *   <li><strong>WebSocket closed:</strong> Connection closed unexpectedly by relay or network</li>
 *   <li><strong>Relay rejected event:</strong> Relay returned ERROR or NOTICE message</li>
 *   <li><strong>DNS failure:</strong> Relay hostname couldn't be resolved</li>
 *   <li><strong>SSL/TLS error:</strong> Certificate validation failed for wss:// relay</li>
 *   <li><strong>Network unreachable:</strong> No internet connection or firewall blocking</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Handling Connection Failures with Retry</h3>
 * <pre>{@code
 * int maxRetries = 3;
 * for (int i = 0; i < maxRetries; i++) {
 *     try {
 *         client.connect(relay);
 *         break; // success
 *     } catch (NostrNetworkException e) {
 *         if (i == maxRetries - 1) {
 *             logger.error("Failed to connect after {} retries: {}", maxRetries, e.getMessage());
 *             throw e;
 *         }
 *         logger.warn("Connection failed, retrying... ({}/{})", i + 1, maxRetries);
 *         Thread.sleep(1000 * (i + 1)); // exponential backoff
 *     }
 * }
 * }</pre>
 *
 * <h3>Example 2: Handling Send Timeouts</h3>
 * <pre>{@code
 * try {
 *     client.send(event, relays); // may timeout if relay is slow
 * } catch (NostrNetworkException e) {
 *     if (e.getMessage().contains("timeout")) {
 *         logger.warn("Send timed out, relay may be slow: {}", relay);
 *         // Retry or use a different relay
 *     } else {
 *         logger.error("Network error: {}", e.getMessage(), e);
 *         throw e;
 *     }
 * }
 * }</pre>
 *
 * <h3>Example 3: Handling Multiple Relays</h3>
 * <pre>{@code
 * List<Relay> successfulRelays = new ArrayList<>();
 * List<Relay> failedRelays = new ArrayList<>();
 *
 * for (Relay relay : relays) {
 *     try {
 *         client.send(event, List.of(relay));
 *         successfulRelays.add(relay);
 *     } catch (NostrNetworkException e) {
 *         logger.warn("Failed to send to {}: {}", relay, e.getMessage());
 *         failedRelays.add(relay);
 *     }
 * }
 *
 * if (successfulRelays.isEmpty()) {
 *     throw new NostrNetworkException("Failed to send to all relays");
 * }
 * logger.info("Event sent to {}/{} relays", successfulRelays.size(), relays.size());
 * }</pre>
 *
 * <h3>Example 4: Handling Relay Errors (OK/NOTICE messages)</h3>
 * <pre>{@code
 * try {
 *     client.send(event, relays);
 * } catch (NostrNetworkException e) {
 *     if (e.getMessage().contains("duplicate")) {
 *         logger.info("Event already exists on relay (not an error)");
 *     } else if (e.getMessage().contains("rate limited")) {
 *         logger.warn("Rate limited by relay, retry later");
 *         Thread.sleep(5000);
 *     } else {
 *         logger.error("Relay rejected event: {}", e.getMessage());
 *         throw e;
 *     }
 * }
 * }</pre>
 *
 * <h2>Recovery Strategies</h2>
 *
 * <ul>
 *   <li><strong>Connection failures:</strong> Retry with exponential backoff, use backup relays</li>
 *   <li><strong>Timeouts:</strong> Increase timeout, use faster relays, implement parallel sends</li>
 *   <li><strong>Relay rejections:</strong> Check event validity, respect rate limits</li>
 *   <li><strong>DNS/SSL errors:</strong> Validate relay URLs, check network configuration</li>
 *   <li><strong>Persistent failures:</strong> Remove bad relays from relay list</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 *
 * <p>Network behavior can be configured via properties:
 * <ul>
 *   <li><strong>nostr.websocket.await-timeout-ms:</strong> Timeout for blocking operations (default 60000)</li>
 *   <li><strong>nostr.websocket.poll-interval-ms:</strong> Polling interval for responses (default 500)</li>
 * </ul>
 *
 * @see nostr.client.springwebsocket.NostrRelayClient
 * @since 0.1.0
 */
@StandardException
public class NostrNetworkException extends NostrRuntimeException {}
