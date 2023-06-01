package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.event.Kind;
import nostr.base.PublicKey;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.UserProfile;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;
import nostr.event.util.Nip05Validator;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Log
@Event(name = "Internet Identifier Metadata Event", nip = 5)
public final class InternetIdentifierMetadataEvent extends GenericEvent {

    public InternetIdentifierMetadataEvent(PublicKey pubKey, TagList tags, @NonNull UserProfile profile) throws NostrException {
        super(pubKey, Kind.SET_METADATA, tags);
        this.init(profile);
    }

    public InternetIdentifierMetadataEvent(PublicKey pubKey, @NonNull UserProfile profile) throws NostrException {
        this(pubKey, new TagList(), profile);
    }

    private void init(UserProfile profile) throws NostrException {
        // NIP-05 validator
        Nip05Validator.builder().nip05(profile.getNip05()).publicKey(getPubKey()).build().validate();

        setContent(profile);
    }
    
    private void setContent(UserProfile profile) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(profile);

            // Escape the JSON string
            String escapedJsonString = escapeJsonString(jsonString);

            this.setContent(escapedJsonString);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
