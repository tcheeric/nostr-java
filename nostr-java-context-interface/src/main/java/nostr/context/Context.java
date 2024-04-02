package nostr.context;

public interface Context {

    void validate();

    public enum Type {
        REQUEST,
        COMMAND
    }
}
