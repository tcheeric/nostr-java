package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import nostr.base.Signature;
import nostr.util.NostrUtil;

public class SignatureDeserializer extends JsonDeserializer<Signature> {
            
    @Override
    public Signature deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode node = objectMapper.readTree(jsonParser);

        String sigValue = node.asText();
        byte[] rawData = NostrUtil.hexToBytes(sigValue);

        Signature signature = new Signature();
        signature.setRawData(rawData);

        return signature;
    }
}
