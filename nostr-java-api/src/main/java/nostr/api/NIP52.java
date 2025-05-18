package nostr.api;

import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.entities.CalendarRsvpContent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

import java.net.URI;
import java.util.List;

import static nostr.api.NIP01.createIdentifierTag;
import static nostr.api.NIP23.createImageTag;
import static nostr.api.NIP23.createSummaryTag;
import static nostr.api.NIP23.createTitleTag;
import static nostr.api.NIP99.createLocationTag;
import static nostr.api.NIP99.createStatusTag;

public class NIP52 extends EventNostr {
    public NIP52(@NonNull Identity sender) {
        setSender(sender);
    }

    @SneakyThrows
    public NIP52 createCalendarTimeBasedEvent(
            @NonNull List<BaseTag> baseTags,
            @NonNull String content,
            @NonNull CalendarContent calendarContent) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.TIME_BASED_CALENDAR_CONTENT, baseTags, content).create();

        genericEvent.addTag(calendarContent.getIdentifierTag());
        genericEvent.addTag(createTitleTag(calendarContent.getTitle()));
        genericEvent.addTag(createStartTag(calendarContent.getStart()));

        if (calendarContent.getGeohashTag() != null) {
            genericEvent.addTag(calendarContent.getGeohashTag());
        }
        if (calendarContent.getEnd() != null) {
            genericEvent.addTag(createEndTag(calendarContent.getEnd()));
        }
        if (calendarContent.getStartTzid() != null) {
            genericEvent.addTag(createStartTzidTag(calendarContent.getStartTzid()));
        }
        if (calendarContent.getEndTzid() != null) {
            genericEvent.addTag(createEndTzidTag(calendarContent.getEndTzid()));
        }
        if (calendarContent.getSummary() != null) {
            genericEvent.addTag(createSummaryTag(calendarContent.getSummary()));
        }
        if (calendarContent.getImage() != null) {
            genericEvent.addTag(createImageTag(URI.create(calendarContent.getImage()).toURL()));
        }
        if (calendarContent.getParticipantPubKeys() != null) {
            calendarContent.getParticipantPubKeys().forEach(p -> {
                genericEvent.addTag(p);
            });
        }
        if (calendarContent.getLocation() != null) {
            genericEvent.addTag(createLocationTag(calendarContent.getLocation()));
        }
        if (calendarContent.getHashtagTags() != null) {
            calendarContent.getHashtagTags().forEach(h -> {
                genericEvent.addTag(h);
            });
        }
        if (calendarContent.getReferenceTags() != null) {
            calendarContent.getReferenceTags().forEach(r -> {
                genericEvent.addTag(r);
            });
        }
        if (calendarContent.getLabelTags() != null) {
            calendarContent.getLabelTags().forEach(l -> {
                genericEvent.addTag(l);
            });
        }

        this.updateEvent(genericEvent);

        return this;
    }

    public NIP52 createCalendarRsvpEvent(
            @NonNull String content,
            @NonNull CalendarRsvpContent calendarRsvpContent) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CALENDAR_EVENT_RSVP, content).create();

        genericEvent.addTag(calendarRsvpContent.getIdentifierTag());
        genericEvent.addTag(calendarRsvpContent.getAddressTag());
        genericEvent.addTag(createStatusTag(calendarRsvpContent.getStatus()));

        if (calendarRsvpContent.getAuthorPubKeyTag() != null) {
            genericEvent.addTag(calendarRsvpContent.getAuthorPubKeyTag());
        }
        if (calendarRsvpContent.getEventTag() != null) {
            genericEvent.addTag(calendarRsvpContent.getEventTag());
        }
        if (calendarRsvpContent.getFbTag() != null) {
            genericEvent.addTag(calendarRsvpContent.getFbTag());
        }

        this.updateEvent(genericEvent);

        return this;
    }

    public NIP52 createDateBasedCalendarEvent(@NonNull String content, @NonNull CalendarContent calendarContent) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.TIME_BASED_CALENDAR_CONTENT, content).create();

        genericEvent.addTag(calendarContent.getIdentifierTag());
        genericEvent.addTag(createTitleTag(calendarContent.getTitle()));
        genericEvent.addTag(createStartTag(calendarContent.getStart()));

        if (calendarContent.getGeohashTag() != null) {
            genericEvent.addTag(calendarContent.getGeohashTag());
        }
        if (calendarContent.getEnd() != null) {
            genericEvent.addTag(createEndTag(calendarContent.getEnd()));
        }
        if (calendarContent.getStartTzid() != null) {
            genericEvent.addTag(createStartTzidTag(calendarContent.getStartTzid()));
        }
        if (calendarContent.getEndTzid() != null) {
            genericEvent.addTag(createEndTzidTag(calendarContent.getEndTzid()));
        }
        if (calendarContent.getSummary() != null) {
            genericEvent.addTag(createSummaryTag(calendarContent.getSummary()));
        }

        this.updateEvent(genericEvent);

        return this;
    }

    public NIP52 addIdentifierTag(@NonNull String identifier) {
        addTag(createIdentifierTag(identifier));
        return this;
    }

    public NIP52 addTitleTag(@NonNull String title) {
        addTag(createTitleTag(title));
        return this;
    }

    public NIP52 addStartTag(@NonNull Long start) {
        addTag(createStartTag(start));
        return this;
    }

    public NIP52 addEndTag(@NonNull Long end) {
        addTag(createEndTag(end));
        return this;
    }

    public NIP52 addEventTag(@NonNull GenericTag eventTag) {
        if (!Constants.Tag.EVENT_CODE.equals(eventTag.getCode())) { // Sanity check
            throw new IllegalArgumentException("tag must be of type EventTag");
        }

        addTag(eventTag);
        return this;
    }

    public static BaseTag createStartTag(@NonNull Long start) {
        return new BaseTagFactory(Constants.Tag.START_CODE, start.toString()).create();
    }

    public static BaseTag createEndTag(@NonNull Long end) {
        return new BaseTagFactory(Constants.Tag.END_CODE, end.toString()).create();
    }

    public static BaseTag createStartTzidTag(@NonNull String startTzid) {
        return new BaseTagFactory(Constants.Tag.START_TZID_CODE, startTzid).create();
    }

    public static BaseTag createEndTzidTag(@NonNull String endTzid) {
        return new BaseTagFactory(Constants.Tag.END_TZID_CODE, endTzid).create();
    }

    public static BaseTag createFreeBusyTag(@NonNull String fb) {
        return new BaseTagFactory(Constants.Tag.FREE_BUSY_CODE, fb).create();
    }

}
