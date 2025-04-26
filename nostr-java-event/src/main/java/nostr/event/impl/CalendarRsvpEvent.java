package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.event.entities.CalendarRsvpContent;
import nostr.event.json.deserializer.CalendarRsvpEventDeserializer;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "CalendarRsvpEvent", nip = 52)
@JsonDeserialize(using = CalendarRsvpEventDeserializer.class)
@NoArgsConstructor
public class CalendarRsvpEvent extends AbstractBaseCalendarEvent<CalendarRsvpContent> {

    public enum Status {
        ACCEPTED("accepted"),
        TENTATIVE("tentative"),
        DECLINED("declined");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        @JsonValue
        public String getStatus() {
            return status;
        }
    }

    public enum FB {
        FREE("free"),
        BUSY("busy");

        private final String value;

        FB(String fb) {
            this.value = fb;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public CalendarRsvpEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content) {
        super(sender, Kind.CALENDAR_RSVP_EVENT, baseTags, content);
    }

    public Status getStatus() {
        CalendarRsvpContent calendarRsvpContent = getCalendarContent();
        return Status.valueOf(calendarRsvpContent.getStatus().toUpperCase());
    }

    public FB getFB() {
        CalendarRsvpContent calendarRsvpContent = getCalendarContent();
        return FB.valueOf(calendarRsvpContent.getFbTag().getAttributes().get(0).getValue().toString().toUpperCase());
    }

    public String getEventId() {
        CalendarRsvpContent calendarRsvpContent = getCalendarContent();
        return calendarRsvpContent.getEventTag().getIdEvent();
    }

    public String getId() {
        CalendarRsvpContent calendarRsvpContent = getCalendarContent();
        return calendarRsvpContent.getIdentifierTag().getId();
    }

    public PublicKey getAuthor() {
        CalendarRsvpContent calendarRsvpContent = getCalendarContent();
        return calendarRsvpContent.getAuthorPubKeyTag().getPublicKey();
    }

    @Override
    protected CalendarRsvpContent getCalendarContent() {
        GenericTag aTag = getTag("a");
        GenericTag identifierTag = getTag("d");

        GenericTag eTag = getTag("e");
        GenericTag fb = getTag("fb");
        GenericTag p = getTag("p");

        CalendarRsvpContent calendarRsvpContent = CalendarRsvpContent.builder(
                GenericTag.convert(identifierTag, IdentifierTag.class),
                GenericTag.convert(aTag, AddressTag.class),
                getTag("status").getAttributes().get(0).getValue().toString()
        ).build();

        if (eTag != null) {
            calendarRsvpContent.setEventTag(GenericTag.convert(eTag, EventTag.class));
        }

        if (fb != null) {
            calendarRsvpContent.setFbTag(fb);
        }

        if (p != null) {
            calendarRsvpContent.setAuthorPubKeyTag(GenericTag.convert(p, PubKeyTag.class));
        }

        return calendarRsvpContent;
    }

    public void validateTags() {
        super.validateTags();

        GenericTag dTag = getTag("d");
        if (dTag == null) {
            throw new AssertionError("Missing \\`d\\` tag for the event identifier.");
        }

        GenericTag aTag = getTag("a");
        if (aTag == null) {
            throw new AssertionError("Missing \\`a\\` tag for the address.");
        }

        GenericTag statusTag = getTag("status");
        if (statusTag == null) {
            throw new AssertionError("Missing \\`status\\` tag for the RSVP status.");
        }
    }
}
