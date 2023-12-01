/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
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

    /**
     * Create a replaceable event
     * @param kind the kind (1000 <= kind < 10000)
     * @param content the content
     * @return 
     */
    public static ReplaceableEvent createReplaceableEvent(@NonNull Integer kind, String content) {
        return new ReplaceableEventFactory(kind, content).create();
    }
    
    /**
     * Create a replaceable event
     * @param tags the note's tags
     * @param kind the kind (1000 <= kind < 10000)
     * @param content the note's content
     * @return 
     */
    public static ReplaceableEvent createReplaceableEvent(@NonNull List<BaseTag> tags, @NonNull Integer kind, String content) {
        return new ReplaceableEventFactory(tags, kind, content).create();
    }

    /**
     * Create an ephemeral event
     * @param kind the kind (20000 <= n < 30000)
     * @param content the note's content
     * @return 
     */
    public static EphemeralEvent createEphemeralEvent(@NonNull Integer kind, String content) {
        return new EphemeralEventFactory(kind, content).create();        
    }    
}
