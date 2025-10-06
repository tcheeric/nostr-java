package nostr.crypto.schnorr;

import lombok.experimental.StandardException;
import nostr.util.exception.NostrCryptoException;

/** Exception thrown when Schnorr signing or verification fails. */
@StandardException
public class SchnorrException extends NostrCryptoException {}
