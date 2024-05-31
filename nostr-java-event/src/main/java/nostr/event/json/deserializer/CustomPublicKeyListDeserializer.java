package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.base.PublicKey;
import nostr.event.list.PublicKeyList;

import java.io.IOException;

public class CustomPublicKeyListDeserializer<T extends PublicKeyList<U>, U extends PublicKey> extends JsonDeserializer<T> {
    private final Class<U> clazz;

    public CustomPublicKeyListDeserializer() {
        this.clazz = (Class<U>) PublicKey.class;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        PublicKeyList<U> publicKeyList = new PublicKeyList<>(clazz);
        JsonNode node = p.readValueAsTree();
        if (node.isArray()) {
            for (JsonNode n : node) {
                String hex = n.asText();
                publicKeyList.add((U) new PublicKey(hex));
            }
        }
        return (T) publicKeyList;
    }
}
