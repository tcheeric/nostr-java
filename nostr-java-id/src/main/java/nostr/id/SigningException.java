package nostr.id;

import lombok.experimental.StandardException;

/**
 * Exception thrown when signing an {@link nostr.base.ISignable} fails.
 */
@StandardException
public class SigningException extends RuntimeException {
}
