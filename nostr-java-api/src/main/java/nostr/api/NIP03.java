/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.impl.OtsEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author eric
 */
public class NIP03 {
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OtsEventFactory extends EventFactory<OtsEvent> {

        private final String ots;
        private List<EventTag> relatedEvents;
        private List<PubKeyTag> relatedPubKeys;

        public OtsEventFactory(String ots, PublicKey sender, String content) {
            super(sender, content);
            this.ots = ots;
        }

        @Override
        public OtsEvent create() {
            var event = new OtsEvent(getSender(), new ArrayList<>(), getContent(), ots);
            relatedEvents.stream().forEach(e -> event.addTag(e));
            relatedPubKeys.stream().forEach(p -> event.addTag(p));
            return event;
        }
        
    }
}
