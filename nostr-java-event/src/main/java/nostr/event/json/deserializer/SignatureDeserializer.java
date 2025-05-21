package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.base.Signature;
import nostr.util.NostrUtil;

import java.io.IOException;

public class SignatureDeserializer extends JsonDeserializer<Signature> {

    @Override
    public Signature deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Signature signature = new Signature();
        signature.setRawData(NostrUtil.hex128ToBytes(jsonParser.getCodec().<JsonNode>readTree(jsonParser).asText()));
        return signature;
    }
}
