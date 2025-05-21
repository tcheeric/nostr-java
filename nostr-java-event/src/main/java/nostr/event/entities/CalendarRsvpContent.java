package nostr.event.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.entities.CalendarRsvpContent.CalendarRsvpContentBuilder;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

@Builder
@JsonDeserialize(builder = CalendarRsvpContentBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class CalendarRsvpContent extends NIP42Content {
    //@JsonProperty
    //private final String id;

    // below fields mandatory
    @Getter
    private final IdentifierTag identifierTag;
    @Getter
    private final AddressTag addressTag;
    @Getter
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

    public Optional<PubKeyTag> getAuthorPubKeyTag() {
        return Optional.ofNullable(authorPubKeyTag);
    }

    public void setAuthorPubKeyTag(@NonNull PubKeyTag authorPubKeyTag) {
        this.authorPubKeyTag = authorPubKeyTag;
    }

    public Optional<EventTag> getEventTag() {
        return Optional.ofNullable(eventTag);
    }

    public void setEventTag(@NonNull EventTag eventTag) {
        this.eventTag = eventTag;
    }

    public Optional<GenericTag> getFbTag() {
        return Optional.ofNullable(fbTag);
    }

    public void setFbTag(@NonNull GenericTag fbTag) {
        this.fbTag = fbTag;
    }
}
