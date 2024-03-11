package nostr.event.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.Reaction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.EventTag;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Reactions", nip = 25)
public class ReactionEvent extends GenericEvent {

    public ReactionEvent(PublicKey pubKey, List<BaseTag> tags, Reaction reaction) {
        super(pubKey, Kind.REACTION, tags, reaction.getEmoji());
    }

    public ReactionEvent(PublicKey pubKey, GenericEvent event, Reaction reaction) {
        super(pubKey, Kind.REACTION);
        this.setContent(reaction.getEmoji());
        this.addTag(EventTag.builder().idEvent(event.getId()).build());
    }

    public ReactionEvent(PublicKey pubKey, GenericEvent event, String content, @NonNull URL emoji) {
        super(pubKey, Kind.REACTION);
        this.setContent(content);
        this.addTag(EventTag.builder().idEvent(event.getId()).build());
        addEmojiTag(content, emoji, getTags());
    }

    public ReactionEvent(PublicKey pubKey, List<BaseTag> tags, String content, @NonNull URL emoji) {
        super(pubKey, Kind.REACTION, tags);
        this.setContent(content);
        addEmojiTag(content, emoji, tags);
    }

    private void addEmojiTag(String content, URL emoji, List<BaseTag> tags) {
        List<ElementAttribute> attributes = new ArrayList<>();
        attributes.add(ElementAttribute.builder().name("shortcode").nip(30).value(content).build());
        attributes.add(ElementAttribute.builder().name("url").nip(30).value(emoji.toString()).build());
        tags.add(new GenericTag("emoji", 30, attributes));
    }
}
