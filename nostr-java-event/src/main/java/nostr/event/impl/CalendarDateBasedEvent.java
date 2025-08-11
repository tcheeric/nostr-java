package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.json.deserializer.CalendarDateBasedEventDeserializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;

import java.util.Date;
import java.util.List;

@Event(name = "Date-Based Calendar Event", nip = 52)
@JsonDeserialize(using = CalendarDateBasedEventDeserializer.class)
@NoArgsConstructor
public class CalendarDateBasedEvent<T extends BaseTag> extends AbstractBaseCalendarEvent<CalendarContent<T>> {

    public CalendarDateBasedEvent(PublicKey sender, List<BaseTag> baseTags, String content) {
        super(sender, Kind.CALENDAR_DATE_BASED_EVENT, baseTags, content);
    }

    public String getId() {
        return getCalendarContent().getIdentifierTag().getUuid();
    }

    public String getTile() {
        return getCalendarContent().getTitle();
    }

    public Date getStart() {
        return new Date(getCalendarContent().getStart());
    }

    public Optional<Date> getEnd() {
        CalendarContent<T> calendarContent = getCalendarContent();
        Optional<Long> end = calendarContent.getEnd();
        return end.map(Date::new);
    }

    public Optional<String> getLocation() {
        return getCalendarContent().getLocation();
    }

    public Optional<String> getGeohash() {
        Optional<GeohashTag> geohashTag = getCalendarContent().getGeohashTag();
        return geohashTag.map(GeohashTag::getLocation);
    }

    public List<PubKeyTag> getParticipants() {
        return getCalendarContent().getParticipantPubKeyTags();
    }

    public List<HashtagTag> getHashtags() {
        return getCalendarContent().getHashtagTags();
    }

    public List<ReferenceTag> getReferences() {
        return getCalendarContent().getReferenceTags();
    }

    @Override
    protected CalendarContent<T> getCalendarContent() {
        CalendarContent<T> calendarContent = new CalendarContent<>(
            (IdentifierTag) getTag("d"),
            ((GenericTag) getTag("title")).getAttributes().get(0).value().toString(),
            Long.parseLong(((GenericTag) getTag("start")).getAttributes().get(0).value().toString())
        );

        // Update the calendarContent object with the values from the tags
        Optional.ofNullable(getTag("end")).ifPresent(baseTag -> 
            calendarContent.setEnd(Long.parseLong(((GenericTag) baseTag).getAttributes().get(0).value().toString())));
        
        Optional.ofNullable(getTag("location")).ifPresent(baseTag ->
            calendarContent.setLocation(((GenericTag) baseTag).getAttributes().get(0).value().toString()));

        Optional.ofNullable(getTag("g")).ifPresent(baseTag -> calendarContent.setGeohashTag((GeohashTag) baseTag));

        Optional.ofNullable(getTags("p")).ifPresent(baseTags -> 
            baseTags.forEach(baseTag -> 
                calendarContent.addParticipantPubKeyTag((PubKeyTag)baseTag)));

        Optional.ofNullable(getTags("t")).ifPresent(baseTags ->
            baseTags.forEach(baseTag ->
                calendarContent.addHashtagTag((HashtagTag) baseTag)));

        Optional.ofNullable(getTags("r")).ifPresent(baseTags ->
            baseTags.forEach(baseTag ->
                calendarContent.addReferenceTag((ReferenceTag) baseTag)));

        return calendarContent;
    }

    @Override
    public void validateKind() {
        if (getKind() != Kind.CALENDAR_DATE_BASED_EVENT.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.CALENDAR_DATE_BASED_EVENT.getValue());
        }
    }
}
