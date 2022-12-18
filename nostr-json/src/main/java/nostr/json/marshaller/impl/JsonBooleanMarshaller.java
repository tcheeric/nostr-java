
package nostr.json.marshaller.impl;

import nostr.json.values.JsonBooleanValue;
import nostr.json.marshaller.BaseMarshaller;

/**
 *
 * @author squirrel
 */
public class JsonBooleanMarshaller extends BaseMarshaller {

    public JsonBooleanMarshaller(JsonBooleanValue jsonBooleanValue) {
        super(jsonBooleanValue);
    }

    @Override
    public String marshall() {
        return getJsonEntityValue().getValue().toString();
    }
    
}
