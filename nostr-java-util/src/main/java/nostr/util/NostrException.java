package nostr.util;

import lombok.experimental.StandardException;

/**
 *
 * @author squirrel
 */
@StandardException
public class NostrException extends Exception {
    public NostrException(String message) {
        super(message);
    }
}
