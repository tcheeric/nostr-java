package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.BaseTag;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EmojiTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.ExpirationTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import nostr.event.tag.NonceTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.event.tag.RelaysTag;
import nostr.event.tag.SubjectTag;
import nostr.event.tag.VoteTag;

import java.io.IOException;

public class TagDeserializer<T extends BaseTag> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        // Extract relevant data from the JSON node
        var code = node.get(0);

        if (code == null) {
            throw new IOException("Unknown tag code: " + null);
        }

        // Perform custom deserialization logic based on the concrete class
        return switch (code.asText()) {
            case "a" -> AddressTag.deserialize(node);
            case "d" -> IdentifierTag.deserialize(node);
            case "e" -> EventTag.deserialize(node);
            case "g" -> GeohashTag.deserialize(node);
            case "l" -> LabelTag.deserialize(node);
            case "L" -> LabelNamespaceTag.deserialize(node);
            case "p" -> PubKeyTag.deserialize(node);
            case "r" -> ReferenceTag.deserialize(node);
            case "t" -> HashtagTag.deserialize(node);
            case "v" -> VoteTag.deserialize(node);
            case "emoji" -> EmojiTag.deserialize(node);
            case "expiration" -> ExpirationTag.deserialize(node);
            case "nonce" -> NonceTag.deserialize(node);
            case "price" -> PriceTag.deserialize(node);
            case "relays" -> RelaysTag.deserialize(node);
            case "subject" -> SubjectTag.deserialize(node);
            default -> (T) new GenericTagDecoder<>().decode(node.toString());
        };

    }
}
