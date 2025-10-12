package nostr.event.json.serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nostr.event.tag.GenericTag;

public class GenericTagSerializer<T extends GenericTag> extends AbstractTagSerializer<T> {

  // Generics are erased at runtime; serializer is intentionally bound to GenericTag.class
  @SuppressWarnings("unchecked")
  public GenericTagSerializer() {
    super((Class<T>) GenericTag.class);
  }

  @Override
  protected void applyCustomAttributes(ObjectNode node, T value) {
    value.getAttributes().forEach(a -> node.put(a.name(), a.value().toString()));
  }
}
