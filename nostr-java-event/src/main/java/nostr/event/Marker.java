package nostr.event;

/**
 *
 * @author squirrel
 */
public enum Marker {
    ROOT("root"),
    REPLY("reply");

    private final String value;

    Marker(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Marker fromValue(String value) {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("root")) {
            return ROOT;
        } else if (value.equalsIgnoreCase("reply")) {
            return REPLY;
        } else {
            throw new IllegalArgumentException("Invalid marker value: " + value);
        }
    }
}
