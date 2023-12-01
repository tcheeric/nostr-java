package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import nostr.event.impl.NostrMarketplaceEvent.Product.Spec;

/**
 *
 * @author eric
 */
public class SpecSerializer extends JsonSerializer<Spec> {

    @Override
    public void serialize(Spec spec, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString(spec.getKey());
        jsonGenerator.writeString(spec.getValue());
        jsonGenerator.writeEndArray();
    }
}
