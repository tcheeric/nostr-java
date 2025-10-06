package nostr.api;

import static nostr.base.json.EventJsonMapper.mapper;
import static nostr.util.NostrUtil.escapeJsonString;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Kind;
import nostr.event.entities.UserProfile;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.util.validator.Nip05Validator;
import nostr.event.json.codec.EventEncodingException;

/**
 * NIP-05 helpers (DNS-based verification). Create internet identifier metadata events.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/05.md">NIP-05</a>
 */
public class NIP05 extends EventNostr {

  public NIP05(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create an Internet Identifier Metadata (IIM) Event
   *
   * @param profile the associate user profile
   * @return the IIM event
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP05 createInternetIdentifierMetadataEvent(@NonNull UserProfile profile) {
    String content = getContent(profile);
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.SET_METADATA.getValue(), new ArrayList<>(), content)
            .create();
    this.updateEvent(genericEvent);
    return this;
  }

  private String getContent(UserProfile profile) {
    try {
      String jsonString =
          mapper().writeValueAsString(
              Nip05Validator.builder()
                  .nip05(profile.getNip05())
                  .publicKey(profile.getPublicKey().toString())
                  .build());
      return escapeJsonString(jsonString);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to encode NIP-05 profile content", ex);
    }
  }
}
