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
public class ProductSerializer extends JsonSerializer<Product> {

    @Override
    public void serialize(Product product, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", product.getId());
        jsonGenerator.writeStringField("stall_id", product.getStall().getId());
        jsonGenerator.writeStringField("name", product.getName());
        if (product.getDescription() != null) {
            jsonGenerator.writeStringField("description", product.getDescription());
        }
        if (!product.getImages().isEmpty()) {
            jsonGenerator.writeFieldName("images");
            jsonGenerator.writeStartArray();
            for (String image : product.getImages()) {
                jsonGenerator.writeString(image);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeStringField("currency", product.getCurrency());
        jsonGenerator.writeNumberField("price", product.getPrice());
        jsonGenerator.writeNumberField("quantity", product.getQuantity());
        if (!product.getSpecs().isEmpty()) {
            jsonGenerator.writeFieldName("specs");
            jsonGenerator.writeStartArray();
            for (Product.Spec spec : product.getSpecs()) {
                jsonGenerator.writeStartArray();
                jsonGenerator.writeString(spec.getKey());
                jsonGenerator.writeString(spec.getValue());
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();
    }
}
