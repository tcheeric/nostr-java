
package com.tcheeric.nostr.json.marshaller;

import com.tcheeric.nostr.json.JsonType;
import com.tcheeric.nostr.json.JsonValue;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@Log
public abstract class BaseMarshaller implements IMarshaller {
    
    private final JsonValue<? extends JsonType> jsonEntityValue;
    private boolean escape;

    public BaseMarshaller(JsonValue<? extends JsonType> jsonEntityValue, boolean escape) {
        this.jsonEntityValue = jsonEntityValue;
        this.escape = escape;
    }

    
    public BaseMarshaller(@NonNull JsonValue<? extends JsonType> jsonEntityValue) {
        this(jsonEntityValue, false);
    }
    
    
}
