package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Represents failures when communicating with relays or external services.
 */
@StandardException
public class NostrNetworkException extends NostrRuntimeException {}
