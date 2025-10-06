package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when serialization or deserialization of Nostr data fails.
 */
@StandardException
public class NostrEncodingException extends NostrRuntimeException {}
