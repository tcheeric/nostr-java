
package nostr.event;

import lombok.Getter;

/**
 *
 * @author squirrel
 */
@Getter
public enum Marker {
    ROOT("root"),
    REPLY("reply"),
    MENTION("mention"),
    FORK("fork");
    
    private final String value;

    Marker(String value) {
        this.value = value;
    }

}