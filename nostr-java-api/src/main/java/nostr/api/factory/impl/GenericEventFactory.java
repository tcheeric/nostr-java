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

  public GenericEventFactory(@NonNull Integer kind) {
    super();
    this.kind = kind;
  }

  public GenericEventFactory(Identity sender, @NonNull Integer kind) {
    super(sender);
    this.kind = kind;
  }

  public GenericEventFactory(@NonNull Integer kind, @NonNull String content) {
    super(null, content);
    this.kind = kind;
  }

  public GenericEventFactory(Identity sender, @NonNull Integer kind, @NonNull String content) {
    super(sender, content);
    this.kind = kind;
  }

  public GenericEventFactory(
      Identity sender, @NonNull Integer kind, List<T> tags, @NonNull String content) {
    super(sender, tags, content);
    this.kind = kind;
  }

  public GenericEvent create() {
    return new GenericEvent(
        getIdentity().getPublicKey(), getKind(), new ArrayList<BaseTag>(getTags()), getContent());
  }
}
