package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Signals violations or inconsistencies with the Nostr protocol or specific NIP specifications.
 */
@StandardException
public class NostrProtocolException extends NostrRuntimeException {}
