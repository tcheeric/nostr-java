package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.entities.NIP15Content;
import nostr.event.tag.GenericTag;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class MerchantEvent<T extends NIP15Content.MerchantContent>
    extends AddressableEvent {

  public MerchantEvent(PublicKey sender, Kind kind, List<BaseTag> tags, String content) {
    this(sender, kind.getValue(), tags, content);
  }

  public MerchantEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
    super(sender, kind, tags, content);
  }

  protected abstract T getEntity();

  @Override
  protected void validateTags() {
    super.validateTags();

    // Check 'd' tag
    BaseTag dTag = getTag("d");
    if (dTag == null) {
      throw new AssertionError("Missing `d` tag.");
    }

    String id = ((GenericTag) dTag).getAttributes().getFirst().value().toString();
    String entityId = getEntity().getId();
    if (!id.equals(entityId)) {
      throw new AssertionError("The d-tag value MUST be the same as the stall id.");
    }
  }

  @Override
  protected void validateContent() {
    super.validateContent();

    try {
      T entity = getEntity();
      if (entity == null) {
        throw new AssertionError("Invalid `content`: Unable to parse merchant entity.");
      }

      if (entity.getId() == null || entity.getId().isEmpty()) {
        throw new AssertionError("Invalid `content`: `id` field is required.");
      }
    } catch (AssertionError e) {
      throw e;
    } catch (Exception e) {
      throw new AssertionError("Invalid `content`: Must be a valid JSON object.", e);
    }
  }
}
