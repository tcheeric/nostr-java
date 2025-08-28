package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.Product;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
@NoArgsConstructor
public abstract class NostrMarketplaceEvent extends AddressableEvent {

  // TODO: Create the Kinds for the events and use it
  public NostrMarketplaceEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
    super(sender, kind, tags, content);
  }

  @SneakyThrows
  public Product getProduct() {
    return IEvent.MAPPER_BLACKBIRD.readValue(getContent(), Product.class);
  }
}
