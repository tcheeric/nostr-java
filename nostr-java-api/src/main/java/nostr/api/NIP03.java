/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.EventFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.OtsEvent;

/**
 *
 * @author eric
 */
public class NIP03 extends Nostr {
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OtsEventFactory extends EventFactory<OtsEvent> {

        private final String ots;

        public OtsEventFactory(String ots, String content) {
            super(content);
            this.ots = ots;
        }

        public OtsEventFactory(List<BaseTag> tags, String ots, String content) {
            super(content);
            this.ots = ots;
        }

        @Deprecated
        public OtsEventFactory(String ots, PublicKey sender, String content) {
            super(sender, content);
            this.ots = ots;
        }

        @Override
        public OtsEvent create() {
            return new OtsEvent(getSender(), getTags(), getContent(), ots);
        }
        
    }
}
