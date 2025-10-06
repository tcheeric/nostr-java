package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.NipConstants;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Replaceable Events")
@NoArgsConstructor
public class ReplaceableEvent extends NIP01Event {

  public ReplaceableEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
    super(sender, kind, tags, content);
  }

  @Override
  protected void validateKind() {
    var n = getKind();
    if ((NipConstants.REPLACEABLE_KIND_MIN <= n && n < NipConstants.REPLACEABLE_KIND_MAX)
        || n == Kind.SET_METADATA.getValue()
        || n == Kind.CONTACT_LIST.getValue()) {
      return;
    }

    throw new AssertionError(
        "Invalid kind value. Must be between %d and %d or equal %d or %d"
            .formatted(
                NipConstants.REPLACEABLE_KIND_MIN,
                NipConstants.REPLACEABLE_KIND_MAX,
                Kind.SET_METADATA.getValue(),
                Kind.CONTACT_LIST.getValue()),
        null);
  }
}
