package nostr.event.impl;

import lombok.EqualsAndHashCode;
import nostr.base.ITag;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.tag.PubKeyTag;

import java.util.List;

/**
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = false)
@Event(name = "Handling Mentions", nip = 8)
public final class MentionsEventNick extends EventDecorator {
  private final GenericEventNick genericEvent;

  public MentionsEventNick(GenericEventNick genericEvent, List<BaseTag> tags, String content) {
    super(genericEvent);
    this.genericEvent = genericEvent;
    setTags(tags);
    setContent(content);
    setKind(Kind.TEXT_NOTE);
  }

  @Override
  public void update() {
    // TODO: refactor procedural into OO
    genericEvent.update();
    int index = 0;
    // TODO - Refactor with the EntityAttributeUtil class
    while (getTags().iterator().hasNext()) {
      ITag tag = getTags().iterator().next();
      String replacement = "#[" + index++ + "]";
      setContent(this.getContent().replace(((PubKeyTag) tag).getPublicKey().toString(), replacement));
    }
  }
}
