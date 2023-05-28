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
import nostr.base.Profile;
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

    private final String name;
    private final String nip05;

    public InternetIdentifierMetadataEvent(PublicKey pubKey, TagList tags, @NonNull Profile profile) {
        super(pubKey, Kind.SET_METADATA, tags);
        this.name = profile.getName();
        this.nip05 = profile.getNip05();
    }

    @Override
    public void update() throws NostrException {

        try {
            // NIP-05 validator
            Nip05Validator.builder().nip05(nip05).publicKey(getPubKey()).build().validate();

            setContent();

            super.update();
        } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }

    }

    // TODO #30 - Use jackson
    private void setContent() {

        try {
            Profile profile = Profile.builder().name(name).nip05(nip05).build();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(profile);

            // Escape the JSON string
            String escapedJsonString = escapeJsonString(jsonString);

            this.setContent(escapedJsonString);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String escapeJsonString(String jsonString) {
        return jsonString.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
