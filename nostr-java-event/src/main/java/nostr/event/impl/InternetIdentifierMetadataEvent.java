package nostr.event.impl;

import static nostr.util.NostrUtil.escapeJsonString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.UserProfile;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.NIP05Event;
import nostr.event.util.Nip05Validator;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Internet Identifier Metadata Event", nip = 5)
public final class InternetIdentifierMetadataEvent extends NIP05Event {

    public InternetIdentifierMetadataEvent(PublicKey pubKey, @NonNull UserProfile profile) {
        super(pubKey, Kind.SET_METADATA);
        this.init(profile);
    }

    private void init(UserProfile profile) {
        try {
            // NIP-05 validator
            Nip05Validator.builder().nip05(profile.getNip05()).publicKey(profile.getPublicKey()).build().validate();

            setContent(profile);
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setContent(UserProfile profile) {

        try {
            String jsonString = MAPPER_AFTERBURNER.writeValueAsString(new Nip05Obj(profile.getName(), profile.getNip05()));

            // Escape the JSON string
            String escapedJsonString = escapeJsonString(jsonString);

            this.setContent(escapedJsonString);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Data
    @AllArgsConstructor
    public static final class Nip05Obj{
    	private String name;
    	private String nip05;
    }
}
