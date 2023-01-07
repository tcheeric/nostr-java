package nostr.event.unmarshaller;

import lombok.Data;
import nostr.base.IElement;
import nostr.base.IUnmarshaller;

/**
 *
 * @author squirrel
 */
@Data
public abstract class BaseElementUnmarshaller implements IUnmarshaller<IElement> {

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
