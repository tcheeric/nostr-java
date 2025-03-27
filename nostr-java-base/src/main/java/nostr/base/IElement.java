package nostr.base;

/**
 *
 * @author squirrel
 */
public interface IElement {

    default Integer getNip() {
        return 1;
    }
}
