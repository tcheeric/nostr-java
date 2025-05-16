package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.base.PublicKey;
import java.io.IOException;

public class PublicKeyDeserializer extends JsonDeserializer<PublicKey> {
    @Override
    public PublicKey deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return new PublicKey(jsonParser.<JsonNode>readValueAsTree().asText());
    }
}
