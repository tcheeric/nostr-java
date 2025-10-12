package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.base.json.EventJsonMapper;
import nostr.event.BaseTag;
import nostr.event.entities.Product;
import nostr.event.json.codec.EventEncodingException;

import java.util.List;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
@NoArgsConstructor
public abstract class NostrMarketplaceEvent extends AddressableEvent {

  /**
   * Creates a new marketplace event.
   *
   * <p>Note: Kind values for marketplace events are defined in NIP-15.
   * Consider using {@link nostr.base.Kind} enum values when available.
   *
   * @param sender the public key of the event creator
   * @param kind the event kind (see NIP-15 for marketplace event kinds)
   * @param tags the event tags
   * @param content the event content (typically JSON-encoded Product)
   */
  public NostrMarketplaceEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
    super(sender, kind, tags, content);
  }

  public Product getProduct() {
    try {
      return EventJsonMapper.mapper().readValue(getContent(), Product.class);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to parse marketplace product content", ex);
    }
  }
}
