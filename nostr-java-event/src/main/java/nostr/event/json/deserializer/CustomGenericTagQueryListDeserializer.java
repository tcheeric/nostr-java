package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import nostr.event.list.GenericTagQueryList;

/**
 *
 * @author eric
 */
public class CustomGenericTagQueryListDeserializer extends JsonDeserializer<GenericTagQueryList> {

    @Override
    public GenericTagQueryList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
