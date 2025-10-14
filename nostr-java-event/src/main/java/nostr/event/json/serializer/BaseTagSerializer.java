package nostr.event.json.serializer;

import nostr.event.BaseTag;

public class BaseTagSerializer<T extends BaseTag> extends AbstractTagSerializer<T> {

  // Generics are erased at runtime; serializer is intentionally bound to BaseTag.class
  @SuppressWarnings("unchecked")
  public BaseTagSerializer() {
    super((Class<T>) BaseTag.class);
  }
}
