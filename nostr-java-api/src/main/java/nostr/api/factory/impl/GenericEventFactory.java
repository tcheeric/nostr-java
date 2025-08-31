package nostr.api.factory.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenericEventFactory<T extends BaseTag> extends EventFactory<GenericEvent, T> {

  private Integer kind;

  /**
   * Create a factory for a given kind with no content and no sender.
   *
   * @param kind the event kind
   */
  public GenericEventFactory(@NonNull Integer kind) {
    super();
    this.kind = kind;
  }

  /**
   * Create a factory for a given kind and sender with no content.
   */
  public GenericEventFactory(Identity sender, @NonNull Integer kind) {
    super(sender);
    this.kind = kind;
  }

  /**
   * Create a factory for a given kind and content with no sender.
   */
  public GenericEventFactory(@NonNull Integer kind, @NonNull String content) {
    super(null, content);
    this.kind = kind;
  }

  /**
   * Create a factory for a given kind, sender and content.
   */
  public GenericEventFactory(Identity sender, @NonNull Integer kind, @NonNull String content) {
    super(sender, content);
    this.kind = kind;
  }

  /**
   * Create a factory for a given kind with sender, tags and content.
   */
  public GenericEventFactory(
      Identity sender, @NonNull Integer kind, List<T> tags, @NonNull String content) {
    super(sender, tags, content);
    this.kind = kind;
  }

  /**
   * Build a GenericEvent with the configured values.
   *
   * @return the new GenericEvent
   */
  public GenericEvent create() {
    return new GenericEvent(
        getIdentity().getPublicKey(), getKind(), new ArrayList<BaseTag>(getTags()), getContent());
  }
}
