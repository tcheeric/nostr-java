/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.impl.NIP33.AddressTagFactory;
import nostr.api.factory.impl.NIP33.IdentifierTagFactory;
import nostr.api.factory.impl.NIP33.ParameterizedReplaceableEventFactory;
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

    public static ParameterizedReplaceableEvent createParameterizedReplaceableEvent(Integer kind, String comment) {
        return new ParameterizedReplaceableEventFactory(kind, comment).create();
    }
    
    public static ParameterizedReplaceableEvent createParameterizedReplaceableEvent(List<BaseTag> tags, Integer kind, String comment) {
        return new ParameterizedReplaceableEventFactory(tags, kind, comment).create();
    }
    
    public static IdentifierTag createIdentifierTag(String id) {
        return new IdentifierTagFactory(id).create();
    }

    public static AddressTag createAddressTag(Integer kind, PublicKey publicKey, IdentifierTag idTag, Relay relay) {
        var result = new AddressTagFactory(publicKey).create();
        result.setIdentifierTag(idTag);
        result.setKind(kind);
        result.setRelay(relay);
        return result;
    }    
}
