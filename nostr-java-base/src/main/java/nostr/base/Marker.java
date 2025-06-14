
package nostr.base;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author squirrel
 */
public enum Marker {
    ROOT("root"),
    REPLY("reply"),
    MENTION("mention"),
    FORK("fork"),
    CREATED("created"),
    DESTROYED("destroyed"),
    REDEEMED("redeemed"),
    READ("read"),
    WRITE("write");

    private final String value;

    Marker(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}