package nostr.id;

import lombok.experimental.StandardException;
import nostr.util.exception.NostrCryptoException;

/** Exception thrown when signing an {@link nostr.base.ISignable} fails. */
@StandardException
public class SigningException extends NostrCryptoException {}
