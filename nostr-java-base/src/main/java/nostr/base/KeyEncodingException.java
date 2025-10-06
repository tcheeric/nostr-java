package nostr.base;

import lombok.experimental.StandardException;

/** Exception thrown when encoding a key to Bech32 fails. */
@StandardException
public class KeyEncodingException extends RuntimeException {}

