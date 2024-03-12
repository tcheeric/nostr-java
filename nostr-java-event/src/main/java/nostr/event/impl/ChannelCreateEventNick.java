package nostr.event.impl;

import lombok.EqualsAndHashCode;
import nostr.base.Profile;
import nostr.base.annotation.Event;
import nostr.event.Kind;

import static nostr.util.NostrUtil.escapeJsonString;

/**
 * @author guilhermegps
 */
@EqualsAndHashCode(callSuper = false)
@Event(name = "Create Channel", nip = 28)
public class ChannelCreateEventNick extends EventDecorator {

  public ChannelCreateEventNick(GenericEventNick genericEvent, Profile profile) {
    super(genericEvent);
    setKind(Kind.CHANNEL_CREATE);
    setContent(escapeJsonString(profile.toString()));
  }
}
