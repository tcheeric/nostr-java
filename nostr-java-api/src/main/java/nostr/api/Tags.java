/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.event.Marker;
import nostr.event.tag.EventTag;
import nostr.event.tag.NIP01Tags;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author eric
 */
@Deprecated(forRemoval = true)
public class Tags {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class NIP01 extends TagFactory<NIP01Tags> {

        public enum TagType {
            Event,
            PubKey
        }

        private final TagType type;

        private IEvent relateEvent;
        private String recommendedRelayUrl;
        private Marker marker;

        private PublicKey publicKey;
        private String mainRelayUrl;
        private String petName;

        public NIP01(TagType type) {
            this.type = type;
        }

        @Override
        public NIP01Tags create() {
            switch (type) {
                case Event -> {
                    return createEventTag(relateEvent, recommendedRelayUrl, marker);
                }
                case PubKey -> {
                    return createPubKeyTag(publicKey, mainRelayUrl, petName);
                }
                default ->
                    throw new AssertionError();
            }
        }

        private static EventTag createEventTag(@NonNull IEvent event, String recommendedRelayUrl, Marker marker) {
            return new EventTag(event.getId(), recommendedRelayUrl, marker);
        }

        private static PubKeyTag createPubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
            return new PubKeyTag(publicKey, mainRelayUrl, petName);
        }
    }

}
