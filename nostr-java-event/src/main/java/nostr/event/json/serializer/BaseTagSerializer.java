package nostr.event.json.serializer;

import java.io.Serial;
import nostr.event.BaseTag;

public class BaseTagSerializer<T extends BaseTag> extends AbstractTagSerializer<T> {

  @Serial private static final long serialVersionUID = -3877972991082754068L;

  @SuppressWarnings("unchecked")
  public BaseTagSerializer() {
    super((Class<T>) BaseTag.class);
  }
}
