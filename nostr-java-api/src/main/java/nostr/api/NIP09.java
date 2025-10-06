package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.Deleteable;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

/**
 * NIP-09 helpers (Event Deletion). Build deletion events targeting events or addresses.
 * Spec: https://github.com/nostr-protocol/nips/blob/master/09.md
 */
public class NIP09 extends EventNostr {

  public NIP09(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a NIP09 Deletion Event
   *
   * @param deleteables an array of event or address tags to be deleted
   * @return this instance for chaining
   */
  public NIP09 createDeletionEvent(@NonNull Deleteable... deleteables) {
    return this.createDeletionEvent(List.of(deleteables));
  }

  /**
   * Create a NIP09 Deletion Event
   *
   * @param deleteables list of event or address tags to be deleted
   * @return this instance for chaining
   */
  public NIP09 createDeletionEvent(@NonNull List<Deleteable> deleteables) {
    List<BaseTag> tags = getTags(deleteables);
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.EVENT_DELETION, tags, "").create();
    this.updateEvent(genericEvent);

    return this;
  }

  private List<BaseTag> getTags(List<Deleteable> deleteables) {
    List<BaseTag> tags = new ArrayList<>();

    // Handle GenericEvents
    deleteables.stream()
        .filter(GenericEvent.class::isInstance)
        .map(GenericEvent.class::cast)
        .forEach(event -> tags.add(new EventTag(event.getId())));

    // Handle AddressTags
    deleteables.stream()
        .filter(GenericEvent.class::isInstance)
        .map(GenericEvent.class::cast)
        .map(GenericEvent::getTags)
        .forEach(
            t ->
                t.stream()
                    .filter(tag -> tag instanceof AddressTag)
                    .map(AddressTag.class::cast)
                    .forEach(
                        tag -> {
                          tags.add(tag);
                          tags.add(NIP25.createKindTag(tag.getKind()));
                        }));

    // Add kind tags for all deleteables
    deleteables.forEach(d -> tags.add(NIP25.createKindTag(d.getKind())));

    return tags;
  }
}
