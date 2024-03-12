
package nostr.event.impl;

import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;

import java.util.List;

/**
 * @author squirrel
 */
@Event(name = "Text Note")
public class TextNoteEventNick extends EventDecorator {

  public TextNoteEventNick(GenericEventNick genericEvent, List<BaseTag> tags, String content) {
    super(genericEvent);
    setKind(Kind.TEXT_NOTE);
    setTags(tags);
    setContent(content);
  }
}
