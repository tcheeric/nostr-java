package nostr.event.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import nostr.base.json.EventJsonMapper;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrException;

/** Utility methods for producing canonical NIP-01 event payloads. */
@UtilityClass
public class EventSerializer {

  private static final ObjectMapper MAPPER = EventJsonMapper.mapper();

  /**
   * Serializes the provided {@link GenericEvent} to its canonical JSON representation following
   * NIP-01.
   *
   * @param event the event to serialize
   * @return the canonical JSON array representation as a string
   * @throws NostrException if the event cannot be serialized
   */
  public static String serializeToJson(@NonNull GenericEvent event) throws NostrException {
    ArrayNode arrayNode = MAPPER.createArrayNode();
    arrayNode.add(0);
    arrayNode.add(Optional.ofNullable(event.getPubKey()).map(Object::toString).orElse(null));
    arrayNode.add(event.getCreatedAt());
    arrayNode.add(event.getKind());
    arrayNode.add(MAPPER.valueToTree(event.getTags()));
    arrayNode.add(event.getContent());

    try {
      return MAPPER.writeValueAsString(arrayNode);
    } catch (JsonProcessingException e) {
      throw new NostrException(e);
    }
  }

  /** Serializes the provided event to UTF-8 encoded bytes of the canonical JSON payload. */
  public static byte[] serializeToBytes(@NonNull GenericEvent event) throws NostrException {
    return serializeToJson(event).getBytes(StandardCharsets.UTF_8);
  }
}
