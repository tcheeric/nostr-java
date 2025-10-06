package nostr.event.impl;

import nostr.base.json.EventJsonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.NIP05Event;
import nostr.event.entities.UserProfile;
import nostr.event.json.codec.EventEncodingException;

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

  public UserProfile getProfile() {
    String content = getContent();
    try {
      return EventJsonMapper.mapper().readValue(content, UserProfile.class);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to parse user profile content", ex);
    }
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

  @Override
  protected void validateKind() {
    if (getKind() != Kind.SET_METADATA.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.SET_METADATA.getValue());
    }
  }
}
