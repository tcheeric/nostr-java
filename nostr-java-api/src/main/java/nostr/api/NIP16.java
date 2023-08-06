/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.EphemeralEvent;
import nostr.event.impl.ReplaceableEvent;

/**
 *
 * @author eric
 */
public class NIP16 extends Nostr {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ReplaceableEventFactory extends EventFactory<ReplaceableEvent> {

        private final Integer kind;
        
        public ReplaceableEventFactory(Integer kind, String content) {
            super(content);
            this.kind = kind;
        }

        public ReplaceableEventFactory(List<BaseTag> tags, Integer kind, String content) {
            super(tags, content);
            this.kind = kind;
        }

        @Override
        public ReplaceableEvent create() {
            return new ReplaceableEvent(getSender(), kind, getTags(), getContent());
        }
    }
    
    public static class EphemeralEventFactory extends EventFactory<EphemeralEvent> {

        private final Integer kind;

        public EphemeralEventFactory(Integer kind, String content) {
            super(content);
            this.kind = kind;
        }

        @Override
        public EphemeralEvent create() {
            return new EphemeralEvent(getSender(), kind, getTags(), getContent());
        }
        
    }
}
