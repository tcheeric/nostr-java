package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.entities.NIP15Content;

/**
 * @author eric
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class CheckoutEvent<T extends NIP15Content.CheckoutContent>
    extends DirectMessageEvent {

  private MessageType messageType;

  public CheckoutEvent(
      PublicKey sender, List<BaseTag> tags, String content, MessageType messageType) {
    super(sender, tags, content);
    this.messageType = messageType;
  }

  public enum MessageType {
    NEW_ORDER(0, "CustomerOrder"),
    PAYMENT_REQUEST(1, "Merchant"),
    ORDER_STATUS_UPDATE(2, "Merchant");

    private final int value;
    @Getter private final String sentBy;

    MessageType(int value, String sentBy) {
      this.value = value;
      this.sentBy = sentBy;
    }

    @JsonValue
    public int getValue() {
      return value;
    }
  }

  protected abstract T getEntity();

  @Override
  protected void validateContent() {
    super.validateContent();

    try {
      T entity = getEntity();
      if (entity == null) {
        throw new AssertionError("Invalid `content`: Must be a valid CustomerOrder JSON object.");
      }

      if (entity.getMessageType() != this.messageType) {
        throw new AssertionError(
            "Invalid `content`: The `messageType` field must match the entity's `messageType`.");
      }

    } catch (Exception e) {
      throw new AssertionError("Invalid `content`: Must be a valid CustomerOrder JSON object.", e);
    }
  }
}
