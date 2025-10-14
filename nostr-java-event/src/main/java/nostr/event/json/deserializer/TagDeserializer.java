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
import nostr.event.tag.UrlTag;
import nostr.event.tag.VoteTag;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class TagDeserializer<T extends BaseTag> extends JsonDeserializer<T> {

  private static final Map<String, Function<JsonNode, ? extends BaseTag>> TAG_DECODERS =
      Map.ofEntries(
          Map.entry("a", AddressTag::deserialize),
          Map.entry("d", IdentifierTag::deserialize),
          Map.entry("e", EventTag::deserialize),
          Map.entry("g", GeohashTag::deserialize),
          Map.entry("l", LabelTag::deserialize),
          Map.entry("L", LabelNamespaceTag::deserialize),
          Map.entry("p", PubKeyTag::deserialize),
          Map.entry("r", ReferenceTag::deserialize),
          Map.entry("t", HashtagTag::deserialize),
          Map.entry("u", UrlTag::deserialize),
          Map.entry("v", VoteTag::deserialize),
          Map.entry("emoji", EmojiTag::deserialize),
          Map.entry("expiration", ExpirationTag::deserialize),
          Map.entry("nonce", NonceTag::deserialize),
          Map.entry("price", PriceTag::deserialize),
          Map.entry("relays", RelaysTag::deserialize),
          Map.entry("subject", SubjectTag::deserialize));

  @Override
  public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    if (!node.isArray() || node.size() == 0 || node.get(0) == null) {
      throw new IOException("Malformed JSON: Expected a non-empty array.");
    }
    String code = node.get(0).asText();

    Function<JsonNode, ? extends BaseTag> decoder = TAG_DECODERS.get(code);
    BaseTag tag =
        decoder != null ? decoder.apply(node) : new GenericTagDecoder<>().decode(node.toString());

    @SuppressWarnings("unchecked")
    T typed = (T) tag;
    return typed;
  }
}
