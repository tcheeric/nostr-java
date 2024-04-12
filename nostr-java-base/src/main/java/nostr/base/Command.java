
package nostr.base;

import lombok.Getter;

/**
 *
 * @author squirrel
 */
@Getter
public enum Command {
    AUTH,
    EVENT,
    REQ,
    CLOSE,
    CLOSED,
    NOTICE,
    EOSE,
    OK
}
