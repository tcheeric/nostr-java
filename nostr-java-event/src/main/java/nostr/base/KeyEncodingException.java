package nostr.base;

import lombok.experimental.StandardException;
import nostr.util.exception.NostrEncodingException;

/** Exception thrown when a key cannot be encoded to the requested format. */
@StandardException
public class KeyEncodingException extends NostrEncodingException {}
