/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.entities.UserProfile;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.util.validator.Nip05Validator;

import java.util.ArrayList;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static nostr.util.NostrUtil.escapeJsonString;

/**
 *
 * @author eric
 */
public class NIP05 extends EventNostr {
	
	public NIP05(@NonNull Identity sender) {
		setSender(sender);
	}
 
    /**
     * Create an Internet Identifier Metadata (IIM) Event
     * @param profile the associate user profile
     * @return the IIM event
     */
    @SneakyThrows
	public NIP05 createInternetIdentifierMetadataEvent(@NonNull UserProfile profile) {
		String content = getContent(profile);
		GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.USER_METADATA, new ArrayList<>(), content).create();
		this.updateEvent(genericEvent);
		return this;
    }

	private String getContent(UserProfile profile) {
		try {
			String jsonString = MAPPER_AFTERBURNER.writeValueAsString(new Nip05Validator(profile.getNip05(), profile.getPublicKey().toString()));
			return escapeJsonString(jsonString);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}


}
