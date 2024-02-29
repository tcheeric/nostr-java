/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;

import lombok.NonNull;
import nostr.api.factory.impl.NIP08Impl.MentionsEventFactory;
import nostr.base.PublicKey;
import nostr.event.NIP08Event;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP08 <T extends NIP08Event> extends EventNostr<T> {
	
	public NIP08(@NonNull Identity sender) {
		setSender(sender);
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

    /**
     * Create a NIP08 mentions event 
     * @param publicKeys the referenced public keys
     * @param content the note's content containing the references to the public keys
     * @return the mentions event
     */
    public NIP08<T> createMentionsEvent(@NonNull List<PublicKey> publicKeys, @NonNull String content) {
    	var event =  new MentionsEventFactory(getSender(), publicKeys, content).create();
    	this.setEvent((T) event);
        
        return this;
    }
}
