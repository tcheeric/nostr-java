package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.base.json.EventJsonMapper;
import nostr.event.BaseTag;
import nostr.event.entities.Stall;
import nostr.event.json.codec.EventEncodingException;

import java.util.List;

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

  public Stall getStall() {
    try {
      return EventJsonMapper.mapper().readValue(getContent(), Stall.class);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to parse stall content", ex);
    }
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
    } catch (EventEncodingException e) {
      throw new AssertionError("Invalid `content`: Must be a valid Stall JSON object.", e);
    }
  }
}
