/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.List;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.MentionsEvent;

/**
 *
 * @author eric
 */
public class NIP08 {

    public static class MentionsEventFactory extends EventFactory<MentionsEvent> {

        public MentionsEventFactory(String content) {
            super(content);
        }

        public MentionsEventFactory(List<BaseTag> tags, String content) {
            super(tags, content);
        }

        @Override
        public MentionsEvent create() {
            var event = new nostr.event.impl.MentionsEvent(getSender(), getTags(), getContent());
            getTags().stream().forEach(t -> event.addTag(t));
            return event;
        }

    }
}
