package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.tag.AddressTag;

import java.io.IOException;

/**
 *
 * @author eric
 */
public class AddressTagSerializer extends JsonSerializer<AddressTag> {

    @Override
    public void serialize(AddressTag value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString("a");
        jsonGenerator.writeString(
            value.getKind() + ":" + 
                value.getPublicKey().toString() + ":" +
                value.getIdentifierTag().getUuid());

        if (value.getRelay() != null) {
            jsonGenerator.writeString("," + value.getRelay().getUri());
        }
        jsonGenerator.writeEndArray();
    }

}
