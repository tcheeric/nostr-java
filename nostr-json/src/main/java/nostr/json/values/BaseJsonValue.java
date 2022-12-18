package nostr.json.values;

import nostr.json.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 * @param <T>
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@Log
public abstract class BaseJsonValue<T> implements JsonValue<T> {

    private final T type;
    private final Object value;

    public BaseJsonValue() {
        this.type = null;
        this.value = null;
    }

    public BaseJsonValue(T type, Object value) {
        this.type = type;
        this.value = value;
    }
}
