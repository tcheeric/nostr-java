package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import nostr.event.tag.ExpirationTag;

/**
 *
 * @author eric
 */
public class ExpirationTagSerializer extends JsonSerializer<ExpirationTag> {

    @Override
    public void serialize(ExpirationTag value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString("expiration");
        jsonGenerator.writeString(Integer.toString(value.getExpiration()));
        jsonGenerator.writeEndArray();
    }
    
}
