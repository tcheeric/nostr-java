package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
    if ((10_000 <= n && n < 20_000) || n == 0 || n == 3) return;

    throw new AssertionError(
        "Invalid kind value. Must be between 10000 and 20000 or egual 0 or 3", null);
  }
}
