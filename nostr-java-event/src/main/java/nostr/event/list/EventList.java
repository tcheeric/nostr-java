package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import nostr.event.BaseEvent;
import nostr.event.json.deserializer.CustomEventListDeserializer;

import java.util.List;

@JsonDeserialize(using = CustomEventListDeserializer.class)
public class EventList<T extends BaseEvent> extends BaseList<T> {
  private final Class<T> clazz;

  public EventList() {
    super();
    this.clazz = (Class<T>) BaseEvent.class;
  }

  public EventList(@NonNull T item) {
    super();
    this.clazz = (Class<T>) BaseEvent.class;
  }

  public EventList(Class<T> clazz) {
    super();
    this.clazz = clazz;
  }

  public EventList(@NonNull List<T> list) {
    this.addAll(list);
    this.clazz = (Class<T>) BaseEvent.class;
  }

  public EventList(@NonNull List<T> list, Class<T> clazz) {
    this.addAll(list);
    this.clazz = clazz;
  }
}
