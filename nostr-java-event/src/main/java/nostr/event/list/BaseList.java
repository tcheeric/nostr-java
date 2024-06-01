package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nostr.base.FNostrList;
import nostr.base.IElement;
import nostr.event.BaseEvent;
import nostr.event.json.serializer.CustomBaseListSerializer;

@JsonSerialize(using = CustomBaseListSerializer.class)
public abstract class BaseList<T extends BaseEvent> extends FNostrList<T> implements IElement {
  @Override
  public Integer getNip() {
    return 1;
  }
}
