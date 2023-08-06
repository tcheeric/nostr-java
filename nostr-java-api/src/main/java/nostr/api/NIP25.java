/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.Reaction;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ReactionEvent;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class NIP25 extends Nostr {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ReactionEventFactory extends EventFactory<ReactionEvent> {

        public final GenericEvent event;
        private final URL emoji;
        
        public ReactionEventFactory(@NonNull GenericEvent event, Reaction reaction) {
            super(reaction.getEmoji());
            this.event = event;
            this.emoji = null;
        }

        public ReactionEventFactory(List<BaseTag> tags, @NonNull GenericEvent event, String reaction) {
            super(tags, reaction);
            this.event = event;
            this.emoji = null;
        }

        public ReactionEventFactory(@NonNull GenericEvent event, String reaction, URL emoji) {
            super(reaction);
            this.event = event;
            this.emoji = emoji;
        }

        public ReactionEventFactory(List<BaseTag> tags, @NonNull GenericEvent event, String reaction, URL emoji) {
            super(tags, reaction);
            this.event = event;
            this.emoji = emoji;
        }

        @Override
        public ReactionEvent create() {
            var reaction = getContent();
            var url = getEmoji();
            
            var reactEvent = new ReactionEvent(getSender(), event, reaction);
            switch (reaction) {
                case "+", "-" -> {
                }
                default -> {
                    var tag = new NIP30.CustomEmojiTagFactory(reaction, url).create();
                    reactEvent.addTag(tag);
                }
            }
            
            return reactEvent;
        }
    }

    public static void like(GenericEvent event) throws NostrException {
        react(event, Reaction.LIKE.getEmoji(), null);
    }

    public static void dislike(GenericEvent event) throws NostrException {
        react(event, Reaction.DISLIKE.getEmoji(), null);
    }
    
    public static void react(GenericEvent event, String reaction, URL url) throws NostrException {
        var reactionEvent = new ReactionEventFactory(event, reaction, url).create();

        Nostr.sign(reactionEvent);
        Nostr.send(reactionEvent);        
    }
}
