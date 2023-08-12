/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.impl.NIP16.EphemeralEventFactory;
import nostr.api.factory.impl.NIP16.ReplaceableEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.EphemeralEvent;
import nostr.event.impl.ReplaceableEvent;

/**
 *
 * @author eric
 */
public class NIP16 extends Nostr {

    public static ReplaceableEvent createReplaceableEvent(Integer kind, String content) {
        return new ReplaceableEventFactory(kind, content).create();
    }
    
    public static ReplaceableEvent createReplaceableEvent(List<BaseTag> tags, Integer kind, String content) {
        return new ReplaceableEventFactory(tags, kind, content).create();
    }

    public static EphemeralEvent createEphemeralEvent(Integer kind, String content) {
        return new EphemeralEventFactory(kind, content).create();        
    }    
}
