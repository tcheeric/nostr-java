/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP08Impl.MentionsEventFactory;
import nostr.event.BaseTag;
import nostr.event.NIP08Event;
import nostr.event.impl.MentionsEvent;
import nostr.id.IIdentity;

import java.util.List;

/**
 *
 * @author eric
 */
@Deprecated(since = "NIP-27")
public class NIP08 <T extends NIP08Event> extends EventNostr<T> {
	
	public NIP08(@NonNull IIdentity sender) {
		setSender(sender);
	}

    /**
     * Create a NIP08 mentions event 
     * @param tags the referenced
     * @param content the note's content containing the references to the public keys
     * @return the mentions event
     */
    public NIP08<T> createMentionsEvent(@NonNull List<BaseTag> tags, @NonNull String content) {
    	var event = new MentionsEvent(getSender().getPublicKey(), tags, content);
    	this.setEvent((T) event);
        
        return this;
    }
    
    /**
     * Create a NIP08 mentions event without pubkey tags
     * @param content the note's content 
     * @return the mentions event without pubkey tags
     */
    public NIP08<T> createMentionsEvent(@NonNull String content) {
    	var event = new MentionsEventFactory(content).create();
    	this.setEvent((T) event);
        
        return this;
    }
}
