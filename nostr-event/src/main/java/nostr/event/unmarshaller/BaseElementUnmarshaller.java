package nostr.event.unmarshaller;

import lombok.Data;
import nostr.base.IUnmarshaller;

/**
 *
 * @author squirrel
 * @param <T>
 */
@Data
public abstract class BaseElementUnmarshaller<T> implements IUnmarshaller<T> {

    private final String json;
    private final boolean escape;

    public BaseElementUnmarshaller(String json) {
        this(json, false);
    }

    public BaseElementUnmarshaller(String json, boolean escape) {
        this.json = json;
        this.escape = escape;
    }

}
