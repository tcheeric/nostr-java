/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.NIP01;
import nostr.api.NIP31;
import nostr.api.factory.EventFactory;
import nostr.base.IEvent;
import nostr.event.impl.OtsEvent;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP03Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OtsEventFactory extends EventFactory<OtsEvent> {

        public OtsEventFactory(@NonNull Identity sender, @NonNull IEvent refEvent, @NonNull String content, @NonNull String alt) {
            super(sender, content);
            this.addTag(NIP31.createAltTag(alt));
            this.addTag(NIP01.createEventTag(refEvent.getId()));
        }

        @Override
        public OtsEvent create() {
            return new OtsEvent(getSender(), getTags(), getContent());
        }

    }

}
