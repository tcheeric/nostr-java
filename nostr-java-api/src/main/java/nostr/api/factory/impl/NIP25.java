/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.net.URL;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.api.factory.impl.NIP30.CustomEmojiTagFactory;
import nostr.event.BaseTag;
import nostr.event.Reaction;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ReactionEvent;

/**
 *
 * @author eric
 */
public class NIP25 {

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
                case "+", "-" -> { // Standard emoji - No tag required
                }
                default -> { // Custom emoji
                    var tag = new CustomEmojiTagFactory(reaction, url).create();
                    reactEvent.addTag(tag);
                }
            }

            return reactEvent;
        }
    }

}
