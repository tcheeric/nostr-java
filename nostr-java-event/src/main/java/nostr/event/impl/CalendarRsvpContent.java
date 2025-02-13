package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.AbstractEventContent;
import nostr.event.impl.CalendarContent.CalendarContentBuilder;
import nostr.event.impl.CalendarRsvpContent.CalendarRsvpContentBuilder;
import nostr.event.tag.AddressTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;

import java.util.List;

@Data
@Builder
@JsonDeserialize(builder = CalendarRsvpContentBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class CalendarRsvpContent extends AbstractEventContent<CalendarRsvpEvent> {
  //@JsonProperty
  private final String id;

  // below fields mandatory
  private final IdentifierTag identifierTag;
  private final AddressTag addressTag;
  private final String status;

  // below fields optional
  private List<PubKeyTag> participantPubKeys;

  public static CalendarRsvpContentBuilder builder(@NonNull IdentifierTag identifierTag, @NonNull AddressTag addressTag, @NonNull String status) {
    return new CalendarRsvpContentBuilder()
        .identifierTag(identifierTag)
        .addressTag(addressTag)
        .status(status);
  }
}
