package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import nostr.base.GenericTagQuery;
import nostr.event.list.GenericTagQueryList;

/**
 *
 * @author eric
 */
public class CustomGenericTagQueryListDeserializer<T extends GenericTagQueryList<U>, U extends GenericTagQuery> extends JsonDeserializer<T> {
    private final Class<U> clazz;

    public CustomGenericTagQueryListDeserializer() {
        this.clazz = (Class<U>) GenericTagQuery.class;
    }

    public CustomGenericTagQueryListDeserializer(Class<U> extendsGenericTagQuery) {
        this.clazz = extendsGenericTagQuery;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
