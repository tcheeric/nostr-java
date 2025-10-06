package nostr.util;

import lombok.experimental.StandardException;
import nostr.util.exception.NostrProtocolException;

/**
 * Legacy exception maintained for backward compatibility. Prefer using specific subclasses of
 * {@link nostr.util.exception.NostrRuntimeException}.
 */
@StandardException
public class NostrException extends NostrProtocolException {
  public NostrException(String message) {
    super(message);
  }
}
