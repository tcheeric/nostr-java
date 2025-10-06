package nostr.crypto.bech32;

import lombok.experimental.StandardException;
import nostr.util.exception.NostrEncodingException;

/** Exception thrown when Bech32 encoding or decoding fails. */
@StandardException
public class Bech32EncodingException extends NostrEncodingException {}
