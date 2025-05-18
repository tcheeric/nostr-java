package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.NIP05Event;
import nostr.event.entities.UserProfile;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Internet Identifier Metadata Event", nip = 5)
@NoArgsConstructor
public final class InternetIdentifierMetadataEvent extends NIP05Event {

    private static final String NAME_PATTERN = "\\w[\\w\\-]+\\w";

    public InternetIdentifierMetadataEvent(PublicKey pubKey, String content) {
        super(pubKey, Kind.SET_METADATA);
        this.setContent(content);
    }

    @SneakyThrows
    public UserProfile getProfile() {
        String content = getContent();
        return MAPPER_AFTERBURNER.readValue(content, UserProfile.class);
    }

    @Override
    protected void validateContent() {
        super.validateContent();

        // Parse and validate the JSON content
        UserProfile profile = getProfile();

        // Validate required fields in the profile
        if (profile.getNip05() == null || profile.getNip05().isEmpty()) {
            throw new AssertionError("Invalid `content`: `nip05` field must not be null or empty.");
        }

        boolean valid = true;
        var strNameArr = profile.getNip05().split("@");
        if (strNameArr.length == 2) {
            var localPart = strNameArr[0];
            valid = localPart.matches(NAME_PATTERN);
        }
        if (!valid) {
            throw new AssertionError("Invalid profile name: " + profile, null);
        }

        // Validate the NIP-05 identifier
        // NOTE: This is now up to the client to perform this validation
    }
}
