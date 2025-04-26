package nostr.event.message;

/**
 *
 * @author eric
 */
public abstract class BaseAuthMessage extends GenericMessage {

    public BaseAuthMessage(String command) {
        super(command);
    }

    @Override
    public Integer getNip() {
        return 42;
    }
}
