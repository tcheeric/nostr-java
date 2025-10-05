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
import nostr.event.entities.Stall;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Create or update a stall", nip = 15)
@NoArgsConstructor
public class CreateOrUpdateStallEvent extends MerchantEvent<Stall> {

  public CreateOrUpdateStallEvent(
      @NonNull PublicKey sender, @NonNull List<BaseTag> tags, @NonNull String content) {
    super(sender, Kind.STALL_CREATE_OR_UPDATE.getValue(), tags, content);
  }

  @SneakyThrows
  public Stall getStall() {
    return IEvent.MAPPER_BLACKBIRD.readValue(getContent(), Stall.class);
  }

  @Override
  protected Stall getEntity() {
    return getStall();
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.STALL_CREATE_OR_UPDATE.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.STALL_CREATE_OR_UPDATE.getValue());
    }
  }

  protected void validateContent() {
    super.validateContent();

    try {
      Stall stall = getStall();

      if (stall.getName() == null || stall.getName().isEmpty()) {
        throw new AssertionError("Invalid `content`: `name` field is required.");
      }
      if (stall.getCurrency() == null || stall.getCurrency().isEmpty()) {
        throw new AssertionError("Invalid `content`: `currency` field is required.");
      }
    } catch (Exception e) {
      throw new AssertionError("Invalid `content`: Must be a valid Stall JSON object.", e);
    }
  }
}
