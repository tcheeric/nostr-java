package nostr.event.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.entities.CalendarRsvpContent.CalendarRsvpContentBuilder;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

@Data
@Builder
@JsonDeserialize(builder = CalendarRsvpContentBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class CalendarRsvpContent extends NIP42Content {
  //@JsonProperty
  //private final String id;

  // below fields mandatory
  private final IdentifierTag identifierTag;
  private final AddressTag addressTag;
  private final String status;

  // below fields optional
  private PubKeyTag authorPubKeyTag;
  private EventTag eventTag;
  private GenericTag fbTag;

  public static CalendarRsvpContentBuilder builder(@NonNull IdentifierTag identifierTag, @NonNull AddressTag addressTag, @NonNull String status) {
    return new CalendarRsvpContentBuilder()
        .identifierTag(identifierTag)
        .addressTag(addressTag)
        .status(status);
  }
}
