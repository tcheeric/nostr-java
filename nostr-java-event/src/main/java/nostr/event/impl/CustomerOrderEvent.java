package nostr.event.impl;

import nostr.base.json.EventJsonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CustomerOrder;
import nostr.event.json.codec.EventEncodingException;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Customer Order Event", nip = 15)
@NoArgsConstructor
public class CustomerOrderEvent extends CheckoutEvent<CustomerOrder> {

  public CustomerOrderEvent(
      @NonNull PublicKey sender, @NonNull List<BaseTag> tags, @NonNull String content) {
    super(sender, tags, content, MessageType.NEW_ORDER);
  }

  public CustomerOrder getCustomerOrder() {
    try {
      return EventJsonMapper.mapper().readValue(getContent(), CustomerOrder.class);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to parse customer order content", ex);
    }
  }

  protected CustomerOrder getEntity() {
    return getCustomerOrder();
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.ENCRYPTED_DIRECT_MESSAGE.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.ENCRYPTED_DIRECT_MESSAGE.getValue());
    }
  }
}
