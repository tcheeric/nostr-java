/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.util.List;

/**
 *
 * @author eric
 */
@Deprecated(since = "NIP-27")
public class NIP08 extends EventNostr {
	
	public NIP08(@NonNull Identity sender) {
		setSender(sender);
	}

    /**
     * Create a NIP08 mentions event 
     * @param tags the referenced
     * @param content the note's content containing the references to the public keys
     * @return the mentions event
     */
    public NIP08 createMentionsEvent(@NonNull Integer kind, @NonNull List<BaseTag> tags, @NonNull String content) {
		GenericEvent genericEvent = new GenericEventFactory(getSender(), kind, tags, content).create();
    	this.updateEvent(genericEvent);
        
        return this;
    }
}
