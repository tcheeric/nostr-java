package nostr.event.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nostr.event.impl.GenericEvent;
import nostr.event.json.EventJsonMapper;
import nostr.util.NostrException;

/**
 * Serializes {@link GenericEvent} instances into the canonical signing array form.
 */
public final class GenericEventSerializer {

  private GenericEventSerializer() {}

  public static String serialize(GenericEvent event) throws NostrException {
    ObjectMapper mapper = EventJsonMapper.getMapper();
    var arrayNode = JsonNodeFactory.instance.arrayNode();
    try {
      arrayNode.add(0);
      arrayNode.add(event.getPubKey().toString());
      arrayNode.add(event.getCreatedAt());
      arrayNode.add(event.getKind());
      arrayNode.add(mapper.valueToTree(event.getTags()));
      arrayNode.add(event.getContent());
      return mapper.writeValueAsString(arrayNode);
    } catch (JsonProcessingException e) {
      throw new NostrException(e.getMessage());
    }
  }
}
