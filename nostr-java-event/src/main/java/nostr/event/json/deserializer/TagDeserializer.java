package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.tag.EventTag;
import nostr.event.tag.NonceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.SubjectTag;
import nostr.util.NostrException;

public class TagDeserializer<T extends BaseTag> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        // Extract relevant data from the JSON node
        String code = node.get(0).asText();

        if (null == code) {
            throw new IOException("Unknown tag code: " + code);
        } else // Perform custom deserialization logic based on the concrete class
        {
            switch (code) {
                case "p" -> {
                    // Deserialization logic for ConcreteTag1
                    PubKeyTag tag = new PubKeyTag();

                    final JsonNode nodePubKey = node.get(1);
                    if (nodePubKey != null) {
                        tag.setPublicKey(new PublicKey(nodePubKey.asText()));
                    }

                    final JsonNode nodeMainUrl = node.get(2);
                    if (nodeMainUrl != null) {
                        tag.setMainRelayUrl(nodeMainUrl.asText());
                    }

                    final JsonNode nodePetName = node.get(3);
                    if (nodePetName != null) {
                        tag.setPetName(nodePetName.asText());
                    }

                    return (T) tag;
                }

                case "nonce" -> {
                    // Deserialization logic for ConcreteTag2
                    NonceTag tag = new NonceTag();

                    final JsonNode nodeNonce = node.get(1);
                    if (nodeNonce != null) {
                        tag.setNonce(Integer.valueOf(nodeNonce.asText()));
                    }

                    final JsonNode nodeDifficulty = node.get(1);
                    if (nodeDifficulty != null) {
                        tag.setDifficulty(Integer.valueOf(nodeDifficulty.asText()));
                    }
                    return (T) tag;
                }
                case "e" -> {
                    // Deserialization logic for ConcreteTag2
                    EventTag tag = new EventTag();

                    final JsonNode nodeIdEvent = node.get(1);
                    if (nodeIdEvent != null) {
                        tag.setIdEvent(nodeIdEvent.asText());
                    }

                    final JsonNode nodeRelay = node.get(2);
                    if (nodeRelay != null) {
                        tag.setRecommendedRelayUrl(nodeRelay.asText());
                    }

                    final JsonNode nodeMarker = node.get(3);
                    if (nodeMarker != null) {
                        tag.setMarker(Marker.valueOf(nodeMarker.asText().toUpperCase()));
                    }
                    return (T) tag;
                }
                case "subject" -> {
                    SubjectTag tag = new SubjectTag();

                    final JsonNode nodeSubject = node.get(1);
                    if (nodeSubject != null) {
                        tag.setSubject(nodeSubject.asText());
                    }
                    return (T) tag;
                }
                default -> {
                    try {
                        var tag = new GenericTagDecoder(node.toString()).decode();
                        return (T) tag;
                    } catch (NostrException ex) {
                        throw new IOException(ex);
                    }
                }

            }
        }
    }
}
