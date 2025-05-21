package nostr.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.entities.CalendarRsvpContent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.id.Identity;
import org.apache.commons.lang3.stream.Streams;
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

    public NIP52 createCalendarTimeBasedEvent(
        @NonNull List<BaseTag> baseTags,
        @NonNull String content,
        @NonNull CalendarContent<BaseTag> calendarContent) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.TIME_BASED_CALENDAR_CONTENT, baseTags, content).create();

        genericEvent.addTag(calendarContent.getIdentifierTag());
        genericEvent.addTag(createTitleTag(calendarContent.getTitle()));
        genericEvent.addTag(createStartTag(calendarContent.getStart()));

        Optional<GeohashTag> geohashTag = calendarContent.getGeohashTag();
        geohashTag.ifPresent(genericEvent::addTag);
        calendarContent.getEnd().ifPresent(aLong -> genericEvent.addTag(createEndTag(aLong)));
        calendarContent.getStartTzid().ifPresent(s -> genericEvent.addTag(createStartTzidTag(s)));
        calendarContent.getEndTzid().ifPresent(s -> genericEvent.addTag(createEndTzidTag(s)));
        calendarContent.getSummary().ifPresent(s -> genericEvent.addTag(createSummaryTag(s)));

        calendarContent.getImage().ifPresent(s ->
            genericEvent.addTag(createImageTag(
                Streams.failableStream(URI.create(s))
                    .map(URI::toURL)
                    .stream().findFirst().orElseThrow())));

        calendarContent.getParticipantPubKeyTags().forEach(genericEvent::addTag);
        calendarContent.getLocation().ifPresent(s -> genericEvent.addTag(createLocationTag(s)));
        calendarContent.getHashtagTags().forEach(genericEvent::addTag);
        calendarContent.getReferenceTags().forEach(genericEvent::addTag);
        calendarContent.getLabelTags().forEach(genericEvent::addTag);

        this.updateEvent(genericEvent);

        return this;
    }

    public NIP52 createCalendarRsvpEvent(
        @NonNull String content,
        @NonNull CalendarRsvpContent calendarRsvpContent) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CALENDAR_EVENT_RSVP, content).create();

//        mandatory tags
        genericEvent.addTag(calendarRsvpContent.getIdentifierTag());
        genericEvent.addTag(calendarRsvpContent.getAddressTag());
        genericEvent.addTag(createStatusTag(calendarRsvpContent.getStatus()));

//        optional tags
        calendarRsvpContent.getAuthorPubKeyTag().ifPresent(genericEvent::addTag);
        calendarRsvpContent.getEventTag().ifPresent(genericEvent::addTag);
        calendarRsvpContent.getFbTag().ifPresent(genericEvent::addTag);

        this.updateEvent(genericEvent);

        return this;
    }

    public NIP52 createDateBasedCalendarEvent(@NonNull String content, @NonNull CalendarContent<BaseTag> calendarContent) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.TIME_BASED_CALENDAR_CONTENT, content).create();

//        mandatory tags
        genericEvent.addTag(calendarContent.getIdentifierTag());
        genericEvent.addTag(createTitleTag(calendarContent.getTitle()));
        genericEvent.addTag(createStartTag(calendarContent.getStart()));

//        optional tags
        calendarContent.getGeohashTag().ifPresent(genericEvent::addTag);
        calendarContent.getEnd().ifPresent(s -> genericEvent.addTag(createEndTag(s)));
        calendarContent.getStartTzid().ifPresent(s -> genericEvent.addTag(createStartTzidTag(s)));
        calendarContent.getEndTzid().ifPresent(s -> genericEvent.addTag(createEndTzidTag(s)));
        calendarContent.getSummary().ifPresent(s -> genericEvent.addTag(createSummaryTag(s)));

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
