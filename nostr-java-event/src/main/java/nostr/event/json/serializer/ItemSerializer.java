package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import nostr.event.entities.CustomerOrder.Item;

/**
 *
 * @author eric
 */
@Deprecated
public class ItemSerializer extends JsonSerializer<Item> {

    @Override
    public void serialize(Item item, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("product_id", item.getProduct().getId());
        jsonGenerator.writeNumberField("quantity", item.getQuantity());
        jsonGenerator.writeEndObject();
    }
}
