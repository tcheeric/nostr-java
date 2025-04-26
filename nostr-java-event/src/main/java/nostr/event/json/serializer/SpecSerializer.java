package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.entities.Product;

import java.io.IOException;

/**
 *
 * @author eric
 */
@Deprecated
public class SpecSerializer extends JsonSerializer<Product.Spec> {

    @Override
    public void serialize(Product.Spec spec, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString(spec.getKey());
        jsonGenerator.writeString(spec.getValue());
        jsonGenerator.writeEndArray();
    }
}
