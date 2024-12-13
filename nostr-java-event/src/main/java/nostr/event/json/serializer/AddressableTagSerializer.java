package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.tag.AddressableTag;

import java.io.IOException;

/**
 *
 * @author eric
 */
public class AddressableTagSerializer extends JsonSerializer<AddressableTag> {

    @Override
    public void serialize(AddressableTag value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString("a");
        jsonGenerator.writeString(value.getKind() + ":" + value.getPublicKey().toString() + ":");

        if(value.getIdentifierTag() != null) {
            jsonGenerator.writeString(value.getIdentifierTag().getId());
        }

        if (value.getRelay() != null) {
            jsonGenerator.writeString("," + value.getRelay().getUri());
        }
        jsonGenerator.writeEndArray();
    }

}
