
package nostr.event;

/**
 *
 * @author squirrel
 */
public enum Marker {
    ROOT("root"),
    REPLY("reply"),
    MENTION("mention");
    
    private final String value;

    Marker(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}