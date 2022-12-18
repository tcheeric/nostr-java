
package nostr.json.marshaller.impl;

import nostr.json.values.JsonNumberValue;
import nostr.json.marshaller.BaseMarshaller;

/**
 *
 * @author squirrel
 */
public class JsonNumberMarshaller extends BaseMarshaller {

    public JsonNumberMarshaller(JsonNumberValue jsonNumberValue) {
        super(jsonNumberValue);
    }

    @Override
    public String marshall() {
        return this.getJsonEntityValue().getValue().toString();
    }
    
}
