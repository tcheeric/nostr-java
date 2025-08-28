package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CustomerOrder;

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

  @SneakyThrows
  public CustomerOrder getCustomerOrder() {
    return IEvent.MAPPER_BLACKBIRD.readValue(getContent(), CustomerOrder.class);
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
