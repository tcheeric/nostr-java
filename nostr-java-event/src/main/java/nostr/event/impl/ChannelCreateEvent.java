package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.entities.ChannelProfile;
import nostr.event.json.codec.EventEncodingException;

/**
 * @author guilhermegps
 */
@Event(name = "Create Channel", nip = 28)
@NoArgsConstructor
public class ChannelCreateEvent extends GenericEvent {

  public ChannelCreateEvent(PublicKey pubKey, String content) {
    super(pubKey, Kind.CHANNEL_CREATE, new ArrayList<>(), content);
  }

  public ChannelProfile getChannelProfile() {
    String content = getContent();
    try {
      return MAPPER_BLACKBIRD.readValue(content, ChannelProfile.class);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to parse channel profile content", ex);
    }
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.CHANNEL_CREATE.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.CHANNEL_CREATE.getValue());
    }
  }

  protected void validateContent() {
    super.validateContent();

    try {
      ChannelProfile profile = getChannelProfile();

      if (profile.getName() == null || profile.getName().isEmpty()) {
        throw new AssertionError("Invalid `content`: `name` field is required.");
      }

      if (profile.getAbout() == null || profile.getAbout().isEmpty()) {
        throw new AssertionError("Invalid `content`: `about` field is required.");
      }

      if (profile.getPicture() == null) {
        throw new AssertionError("Invalid `content`: `picture` field is required.");
      }

    } catch (EventEncodingException e) {
      throw new AssertionError("Invalid `content`: Must be a valid ChannelProfile JSON object.", e);
    }
  }
}
