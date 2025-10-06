package nostr.crypto.bech32;

import lombok.experimental.StandardException;

/** Exception thrown when Bech32 encoding or decoding fails. */
@StandardException
public class Bech32EncodingException extends RuntimeException {}

