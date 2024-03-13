/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP05Impl.InternetIdentifierMetadataEventFactory;
import nostr.base.UserProfile;
import nostr.event.NIP05Event;
import nostr.id.IIdentity;

/**
 *
 * @author eric
 */
public class NIP05<T extends NIP05Event> extends EventNostr<T> {
	
	public NIP05(@NonNull IIdentity sender) {
		setSender(sender);
	}
 
    /**
     * Create an Internet Identifier Metadata (IIM) Event
     * @param profile the associate user profile
     * @return the IIM event
     */
    public NIP05<T> createInternetIdentifierMetadataEvent(@NonNull UserProfile profile) {
    	var event = new InternetIdentifierMetadataEventFactory(getSender(), profile).create();
		this.setEvent((T) event);

		return this;
    }
}
