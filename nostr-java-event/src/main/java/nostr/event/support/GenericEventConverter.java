package nostr.event.support;

import java.lang.reflect.InvocationTargetException;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrException;

/**
 * Converts {@link GenericEvent} instances to concrete event subtypes.
 */
public final class GenericEventConverter {

  private GenericEventConverter() {}

  public static <T extends GenericEvent> T convert(
      @NonNull GenericEvent source, @NonNull Class<T> target) throws NostrException {
    try {
      T event = target.getConstructor().newInstance();
      event.setContent(source.getContent());
      event.setTags(source.getTags());
      event.setPubKey(source.getPubKey());
      event.setId(source.getId());
      event.set_serializedEvent(source.get_serializedEvent());
      event.setNip(source.getNip());
      event.setKind(source.getKind());
      event.setSignature(source.getSignature());
      event.setCreatedAt(source.getCreatedAt());
      return event;
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new NostrException("Failed to convert GenericEvent", e);
    }
  }
}
