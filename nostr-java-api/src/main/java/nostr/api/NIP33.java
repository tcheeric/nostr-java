/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.TagFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.ParameterizedReplaceableEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;

/**
 *
 * @author eric
 */
public class NIP33 extends Nostr {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ParameterizedReplaceableEventFactory extends EventFactory<ParameterizedReplaceableEvent> {

        private final Integer kind;
        
        public ParameterizedReplaceableEventFactory(@NonNull List<BaseTag> tags, Integer kind, String comment) {
            super(tags, comment);
            this.kind = kind;
        }

        @Override
        public ParameterizedReplaceableEvent create() {
            return new ParameterizedReplaceableEvent(getSender(), kind, getTags(), getContent());
        }        
    }
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class IdentifierTagFactory extends TagFactory<IdentifierTag> {

        private final String id;

        public IdentifierTagFactory(String id) {
            this.id = id;
        }

        @Override
        public IdentifierTag create() {
            return new IdentifierTag(id);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class AddressTagFactory extends TagFactory<AddressTag> {

        private Integer kind;
        private final PublicKey publicKey;
        private IdentifierTag identifierTag;
        private Relay relay;

        public AddressTagFactory(@NonNull PublicKey publicKey) {
            this.publicKey = publicKey;
        }

        @Override
        public AddressTag create() {
            return new AddressTag(kind, publicKey, identifierTag, relay);
        }
    }
}
