package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
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
public class CalendarDateBasedEvent extends AbstractBaseCalendarEvent<CalendarContent> {

    public CalendarDateBasedEvent(PublicKey sender, List<BaseTag> baseTags, String content) {
        super(sender, Kind.CALENDAR_DATE_BASED_EVENT, baseTags, content);
    }

    public String getId() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getIdentifierTag().getId();
    }

    public String getTile() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getTitle();
    }

    public Date getStart() {
        CalendarContent calendarContent = getCalendarContent();
        return new Date(calendarContent.getStart());
    }

    public Date getEnd() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getEnd() != null ? new Date(calendarContent.getEnd()) : null;
    }

    public String getLocation() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getLocation() != null ? calendarContent.getLocation() : null;
    }

    public String getGeohash() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getGeohashTag() != null ? calendarContent.getGeohashTag().getLocation() : null;
    }

    public List<PublicKey> getParticipants() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getParticipantPubKeys() != null ? calendarContent.getParticipantPubKeys().stream().map(p -> p.getPublicKey()).toList() : null;
    }

    public List<String> getHashtags() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getHashtagTags() != null ? calendarContent.getHashtagTags().stream().map(h -> h.getHashTag()).toList() : null;
    }

    public List<String> getReferences() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getReferenceTags() != null ? calendarContent.getReferenceTags().stream().map(r -> r.getUri().toString()).toList() : null;
    }

    @Override
    protected CalendarContent getCalendarContent() {

        GenericTag identifierTag = getTag("d");
        GenericTag titleTag = getTag("title");
        GenericTag startTag = getTag("start");

        CalendarContent calendarContent = CalendarContent.builder(
                GenericTag.convert(identifierTag, IdentifierTag.class),
                titleTag.getAttributes().get(0).getValue().toString(),
                Long.parseLong(startTag.getAttributes().get(0).getValue().toString())
        ).build();

        GenericTag endTag = getTag("end");
        GenericTag locationTag = getTag("location");
        GenericTag gTag = getTag("g");
        List<GenericTag> pTags = getTags("p");
        List<GenericTag> tTags = getTags("t");
        List<GenericTag> rTags = getTags("r");

        // Update the calendarContent object with the values from the tags
        if (endTag != null) {
            calendarContent.setEnd(Long.parseLong(endTag.getAttributes().get(0).getValue().toString()));
        }

        if (locationTag != null) {
            calendarContent.setLocation(locationTag.getAttributes().get(0).getValue().toString());
        }

        if (gTag != null) {
            calendarContent.setGeohashTag(GenericTag.convert(identifierTag, GeohashTag.class));
        }

        if (pTags != null) {
            for (GenericTag pTag : pTags) {
                calendarContent.addParticipantPubKey(GenericTag.convert(pTag, PubKeyTag.class));
            }
        }

        if (tTags != null) {
            for (GenericTag tTag : tTags) {
                calendarContent.addHashtagTag(GenericTag.convert(tTag, HashtagTag.class));
            }
        }

        if (rTags != null) {
            for (GenericTag rTag : rTags) {
                calendarContent.addReferenceTag(GenericTag.convert(rTag, ReferenceTag.class));
            }
        }

        return calendarContent;
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        GenericTag dTag = getTag("d");
        if (dTag == null) {
            throw new AssertionError("Missing `d` tag for the event identifier.");
        }

        GenericTag titleTag = getTag("title");
        if (titleTag == null) {
            throw new AssertionError("Missing `title` tag for the event title.");
        }

        GenericTag startTag = getTag("start");
        if (startTag == null) {
            throw new AssertionError("Missing `start` tag with a valid start timestamp.");
        }

        try {
            Long.parseLong(startTag.getAttributes().get(0).getValue().toString());
        } catch (NumberFormatException e) {
            throw new AssertionError("Invalid `start` tag value: must be a numeric timestamp.");
        }
    }

}
