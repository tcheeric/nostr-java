/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.OtsEvent;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP03 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OtsEventFactory extends EventFactory<OtsEvent> {

        private final String ots;

        public OtsEventFactory(@NonNull String ots, @NonNull String content) {
            super(content);
            this.ots = ots;
        }

        public OtsEventFactory(@NonNull Identity sender, @NonNull String ots, @NonNull String content) {
            super(sender, content);
            this.ots = ots;
        }

        public OtsEventFactory(@NonNull List<BaseTag> tags, @NonNull String ots, @NonNull String content) {
            super(content);
            this.ots = ots;
        }

        public OtsEventFactory(@NonNull Identity sender, @NonNull List<BaseTag> tags, @NonNull String ots, @NonNull String content) {
            super(sender, content);
            this.ots = ots;
        }

        @Override
        public OtsEvent create() {
            return new OtsEvent(getSender(), getTags(), getContent(), ots);
        }

    }

}
