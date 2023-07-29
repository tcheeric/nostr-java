package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import nostr.event.tag.AddressTag;

/**
 *
 * @author eric
 */
public class AddressTagSerializer extends JsonSerializer<AddressTag> {

    @Override
    public void serialize(AddressTag value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString("a");
        jsonGenerator.writeString(value.getKind() + ":" + value.getPublicKey().toString() + ":" + value.getIdentifierTag().getId());
        if (value.getRelay() != null) {
            jsonGenerator.writeString(value.getRelay().getUri());
        }
        jsonGenerator.writeEndArray();
    }

}
