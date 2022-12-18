package com.tcheeric.nostr.json.values;

import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.marshaller.impl.JsonExpressionMarshaller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author squirrel
 * @param <T>
 */
@Builder
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class JsonExpression<T> implements JsonValue<T> {
    
    private String variable;
    
    private JsonValue<T> jsonValue;        

    @Override
    public String toString() {
        return new JsonExpressionMarshaller(this).marshall();
    }

    public String toString(boolean escape) {
        return new JsonExpressionMarshaller(this, escape).marshall();
    }

    @Override
    public JsonValue<T> getValue() {
        return jsonValue;
    }
}
