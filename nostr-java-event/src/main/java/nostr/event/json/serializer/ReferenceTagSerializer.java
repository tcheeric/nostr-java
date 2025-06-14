package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.tag.ReferenceTag;

import java.io.IOException;

/**
 * @author eric
 */
public class ReferenceTagSerializer extends JsonSerializer<ReferenceTag> {

    @Override
    public void serialize(ReferenceTag refTag, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString("r");
        jsonGenerator.writeString(refTag.getUri().toString());
        if (refTag.getMarker() != null) {
            jsonGenerator.writeString(refTag.getMarker().getValue());
        }
        jsonGenerator.writeEndArray();
    }

}
