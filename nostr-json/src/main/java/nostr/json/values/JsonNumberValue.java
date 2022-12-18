package nostr.json.values;

import nostr.json.marshaller.impl.JsonNumberMarshaller;
import nostr.json.types.JsonNumberType;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class JsonNumberValue extends BaseJsonValue<JsonNumberType> {

    private JsonNumberValue(JsonNumberType type, Number value) {
        super(type, value);
    }

    public JsonNumberValue(Number value) {
        this(new JsonNumberType(), value);
    }

    @Override
    public Number getValue() {
        return (Number) super.getValue();
    }

    @Override
    public String toString() {
        return new JsonNumberMarshaller(this).marshall();
    }

    public Integer intValue() {
        return doubleValue().intValue();
    }

    public Double doubleValue() {
        return ((Double) getValue());
    }

    public Long longValue() {
        return ((Long) getValue());
    }

}
