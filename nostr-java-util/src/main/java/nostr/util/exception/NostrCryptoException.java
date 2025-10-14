package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when cryptographic operations fail (signing, verification, key generation, encryption).
 *
 * <p>This exception indicates that a cryptographic operation could not be completed successfully.
 * It wraps underlying crypto library exceptions and provides context about what failed in the
 * Nostr SDK.
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li><strong>Signing failed:</strong> BIP-340 Schnorr signing couldn't compute signature</li>
 *   <li><strong>Verification failed:</strong> Signature verification detected invalid signature</li>
 *   <li><strong>Key generation failed:</strong> Unable to generate valid key pair</li>
 *   <li><strong>Invalid private key:</strong> Private key is malformed or out of range</li>
 *   <li><strong>ECDH failed:</strong> Elliptic curve Diffie-Hellman key agreement failed</li>
 *   <li><strong>Encryption/decryption failed:</strong> NIP-04 or NIP-44 encryption operation failed</li>
 *   <li><strong>Key derivation failed:</strong> HMAC-SHA256 or other key derivation failed</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Handling Signing Failures</h3>
 * <pre>{@code
 * try {
 *     GenericEvent event = new GenericEvent(pubKey, Kind.TEXT_NOTE);
 *     event.setContent("Hello Nostr!");
 *     identity.sign(event); // might throw if private key is invalid
 * } catch (NostrCryptoException e) {
 *     logger.error("Failed to sign event: {}", e.getMessage(), e);
 *     // Check if private key is valid
 * }
 * }</pre>
 *
 * <h3>Example 2: Handling Verification Failures</h3>
 * <pre>{@code
 * try {
 *     boolean valid = event.verify(); // returns false for invalid signatures
 *     if (!valid) {
 *         throw new NostrCryptoException("Event signature verification failed");
 *     }
 * } catch (NostrCryptoException e) {
 *     logger.warn("Invalid signature from {}: {}", event.getPubKey(), e.getMessage());
 *     // Reject the event
 * }
 * }</pre>
 *
 * <h3>Example 3: Handling Encryption Failures</h3>
 * <pre>{@code
 * try {
 *     String encrypted = NIP44.encrypt(identity, "secret message", recipientPubKey);
 * } catch (NostrCryptoException e) {
 *     logger.error("Encryption failed: {}", e.getMessage(), e);
 *     // Check if recipient public key is valid
 * }
 * }</pre>
 *
 * <h2>Recovery Strategies</h2>
 *
 * <ul>
 *   <li><strong>Signing failures:</strong> Validate the private key format, regenerate identity if corrupted</li>
 *   <li><strong>Verification failures:</strong> Reject the event, don't trust unverified content</li>
 *   <li><strong>Key generation:</strong> Retry with proper entropy source</li>
 *   <li><strong>Encryption failures:</strong> Validate public keys, check algorithm compatibility</li>
 * </ul>
 *
 * <h2>Security Implications</h2>
 *
 * <ul>
 *   <li><strong>Never ignore crypto exceptions:</strong> They indicate security-critical failures</li>
 *   <li><strong>Log failures:</strong> Crypto exceptions may indicate attacks (signature forgery attempts)</li>
 *   <li><strong>Fail secure:</strong> Reject events/operations on crypto failures (don't proceed)</li>
 *   <li><strong>Key protection:</strong> Ensure private keys are stored securely to prevent failures</li>
 * </ul>
 *
 * @see nostr.crypto.schnorr.Schnorr
 * @see nostr.id.Identity#sign(nostr.event.impl.GenericEvent)
 * @see nostr.event.impl.GenericEvent#verify()
 * @since 0.1.0
 */
@StandardException
public class NostrCryptoException extends NostrRuntimeException {}
