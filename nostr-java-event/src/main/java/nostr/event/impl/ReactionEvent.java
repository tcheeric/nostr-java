package nostr.event.impl;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP25Event;
import nostr.event.Reaction;
import nostr.event.tag.EventTag;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Reactions", nip = 25)
public class ReactionEvent extends NIP25Event {

    public ReactionEvent(PublicKey pubKey, List<BaseTag> tags, Reaction reaction) {
        super(pubKey, Kind.REACTION, tags, reaction.getEmoji());
    }

    public ReactionEvent(PublicKey pubKey, GenericEvent event, Reaction reaction) {
        super(pubKey, Kind.REACTION);
        this.setContent(reaction.getEmoji());
        this.addTag(EventTag.builder().idEvent(event.getId()).build());
    }

    public ReactionEvent(PublicKey pubKey, GenericEvent event, List<BaseTag> tags, String content) {
        super(pubKey, Kind.REACTION, tags);
        this.setContent(content);
        if(event != null)
        	this.addTag(EventTag.builder().idEvent(event.getId()).build());
    }

    public ReactionEvent(PublicKey pubKey, String idEvent, String content) {
        super(pubKey, Kind.REACTION);
        this.setContent(content);
        this.addTag(EventTag.builder().idEvent(idEvent).build());
    }
}
