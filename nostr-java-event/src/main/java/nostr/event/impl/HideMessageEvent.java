package nostr.event.impl;

import java.util.List;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.EventTag;

/**
 * @author guilhermegps
 */
@Event(name = "Hide Message on Channel", nip = 28)
@NoArgsConstructor
public class HideMessageEvent extends GenericEvent {

  public HideMessageEvent(PublicKey pubKey, List<BaseTag> tags, String content) {
    super(pubKey, Kind.HIDE_MESSAGE, tags, content);
  }

  public String getHiddenMessageEventId() {
    return getTags().stream()
        .filter(tag -> "e".equals(tag.getCode()))
        .map(tag -> (EventTag) tag)
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing or invalid `e` root tag."))
        .getIdEvent();
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // Validate `tags` field for at least one `e` tag
    boolean hasEventTag = this.getTags().stream().anyMatch(tag -> tag instanceof EventTag);
    if (!hasEventTag) {
      throw new AssertionError("Invalid `tags`: Must include at least one `e` tag.");
    }
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.HIDE_MESSAGE.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.HIDE_MESSAGE.getValue());
    }
  }
}
