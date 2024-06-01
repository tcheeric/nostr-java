package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.list.PublicKeyList;

import java.io.IOException;

@NoArgsConstructor
public class CustomPublicKeyListDeserializer extends JsonDeserializer<PublicKeyList> {

    @Override
    // TODO: check ctxt use
    public PublicKeyList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        PublicKeyList publicKeyList = new PublicKeyList();
        JsonNode node = p.readValueAsTree();
        if (node.isArray()) {
            for (JsonNode n : node) {
                String hex = n.asText();
                publicKeyList.add(new PublicKey(hex));
            }
        }
        return publicKeyList;
    }
}
