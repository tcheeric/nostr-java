package nostr.util.exception;

import lombok.experimental.StandardException;

/**
 * Indicates failures in cryptographic operations such as signing, verification, or key generation.
 */
@StandardException
public class NostrCryptoException extends NostrRuntimeException {}
