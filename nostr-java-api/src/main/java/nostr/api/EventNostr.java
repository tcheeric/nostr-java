/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * @author guilhermegps
 */
public abstract class EventNostr<T extends GenericEvent> extends Nostr {

	@Getter
	@Setter
	private T event;
	
	@Getter
	private PublicKey recipient;

	public EventNostr sign() {
		super.sign(getSender(), event);

		return this;
    }

	public T send() {
		super.send(this.event);

		return this.event;
	}

	public T send(Map<String, String> relays) {
		super.setRelays(relays);

		return send();
	}
	
	public EventNostr setSender(@NonNull Identity sender) {
		super.setSender(sender);
		
		return this;
	}
	
	public EventNostr setRecipient(@NonNull PublicKey recipient) {
		this.recipient = recipient;
		
		return this;
	}
}
