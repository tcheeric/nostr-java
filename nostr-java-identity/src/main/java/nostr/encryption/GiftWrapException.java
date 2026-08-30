package nostr.encryption;

import lombok.experimental.StandardException;
import nostr.util.exception.NostrCryptoException;

/**
 * Signals that an event could not be gift wrapped, or that an incoming gift wrap was rejected.
 *
 * <p>Rejection is not always a fault. A subscription for kind-1059 events delivers wraps
 * addressed to other recipients, and those cannot be opened by design. Callers reading an inbox
 * should skip a wrap that raises this rather than abandoning the batch, so that one unopenable
 * or malicious event cannot stall an entire conversation.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 */
@StandardException
public class GiftWrapException extends NostrCryptoException {}
