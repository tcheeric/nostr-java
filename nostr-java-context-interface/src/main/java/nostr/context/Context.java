package nostr.context;

public interface Context {

    void validate();

    enum Type {
        REQUEST,
        COMMAND
    }
}
