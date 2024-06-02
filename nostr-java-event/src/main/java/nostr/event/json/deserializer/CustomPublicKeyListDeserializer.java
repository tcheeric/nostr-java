package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class CustomPublicKeyListDeserializer extends JsonDeserializer<List<PublicKey>> {

    @Override
    // TODO: check ctxt use
    public List<PublicKey> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<PublicKey> publicKeyList = new ArrayList<>();
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
