package nostr.event;

/**
 *
 * @author squirrel
 */
public enum Reaction {
    LIKE("+"),
    DISLIKE("-");

    private final String emoji;

    Reaction(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
