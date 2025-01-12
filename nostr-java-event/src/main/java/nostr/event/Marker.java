
package nostr.event;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

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
    REDEEMED("redeemed");

    private final String value;

    Marker(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}