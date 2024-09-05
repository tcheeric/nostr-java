package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.BaseTag;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.NonceTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.RelaysTag;
import nostr.event.tag.SubjectTag;

import java.io.IOException;

public class TagDeserializer<T extends BaseTag> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        // Extract relevant data from the JSON node
        String code = node.get(0).asText();

        if (null == code) {
            throw new IOException("Unknown tag code: " + null);
        } else // Perform custom deserialization logic based on the concrete class
        {
            return switch (code) {
                case "d" -> IdentifierTag.deserialize(node);
                case "e" -> EventTag.deserialize(node);
                case "g" -> GeohashTag.deserialize(node);
                case "p" -> PubKeyTag.deserialize(node);
                case "t" -> HashtagTag.deserialize(node);

                case "nonce" -> NonceTag.deserialize(node);
                case "price" -> PriceTag.deserialize(node);
                case "relays" -> RelaysTag.deserialize(node);
                case "subject" -> SubjectTag.deserialize(node);
                default -> (T) new GenericTagDecoder<>().decode(node.toString());
            };
        }
    }
}
