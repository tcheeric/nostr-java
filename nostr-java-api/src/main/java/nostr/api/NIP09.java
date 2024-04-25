/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP09Impl.DeletionEventFactory;
import nostr.event.BaseTag;
import nostr.event.NIP09Event;
import nostr.id.Identity;

import java.util.List;

/**
 *
 * @author eric
 */
public class NIP09<T extends NIP09Event> extends EventNostr<T> {
	
	public NIP09(@NonNull Identity sender) {
		setSender(sender);
	}

    /**
     * Create a NIP09 Deletion Event
     *
     * @param tags list of event or address tags to be deleted
     * @return the deletion event
     */
    public NIP09<T> createDeletionEvent(@NonNull List<BaseTag> tags) {
    	var event = new DeletionEventFactory(getSender(), tags).create();
		this.setEvent((T) event);

		return this;
    }
}
