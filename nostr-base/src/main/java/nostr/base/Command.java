
package nostr.base;

/**
 *
 * @author squirrel
 */
public enum Command {
    EVENT("IN/OUT"),
    REQ("OUT"),
    CLOSE("OUT"),
    NOTICE("IN"),
    EOSE("IN"),
    OK("IN");

    public static final String DIRECTION_IN = "IN";
    public static final String DIRECTION_OUT = "OUT";
    public static final String DIRECTION_IN_OUT = "IN/OUT";

    private final String direction;

    private Command(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }        
}
