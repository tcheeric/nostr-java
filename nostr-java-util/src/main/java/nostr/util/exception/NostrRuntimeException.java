package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Base unchecked exception for all Nostr domain errors surfaced by the SDK. Subclasses provide
 * additional context for protocol, cryptography, encoding, and networking failures.
 */
@StandardException
public class NostrRuntimeException extends RuntimeException {}
