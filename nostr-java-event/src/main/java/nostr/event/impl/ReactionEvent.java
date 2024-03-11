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

/**
 *
 * @author squirrel
 */
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

    public ReactionEvent(PublicKey pubKey, GenericEvent event, String content) {
        super(pubKey, Kind.REACTION);
        this.setContent(content);
        this.addTag(EventTag.builder().idEvent(event.getId()).build());
    }

    public ReactionEvent(PublicKey pubKey, String idEvent, String content) {
        super(pubKey, Kind.REACTION);
        this.setContent(content);
        this.addTag(EventTag.builder().idEvent(idEvent).build());
    }

    public ReactionEvent(PublicKey pubKey, List<BaseTag> tags, String content) {
        super(pubKey, Kind.REACTION, tags);
        this.setContent(content);
    }

//    private void addEmojiTag(String content, URL emoji, List<BaseTag> tags) {
//        List<ElementAttribute> attributes = new ArrayList<>();
//        attributes.add(ElementAttribute.builder().name("shortcode").nip(30).value(content).build());
//        attributes.add(ElementAttribute.builder().name("url").nip(30).value(emoji.toString()).build());
//        tags.add(new GenericTag("emoji", 30, attributes));
//    }
}
