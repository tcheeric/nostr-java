package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.Product;
import nostr.event.json.codec.EventEncodingException;

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

  public Product getProduct() {
    try {
      return IEvent.MAPPER_BLACKBIRD.readValue(getContent(), Product.class);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to parse marketplace product content", ex);
    }
  }
}
