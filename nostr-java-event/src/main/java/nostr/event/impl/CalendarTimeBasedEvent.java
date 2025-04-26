package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.event.entities.CalendarContent;
import nostr.event.json.deserializer.CalendarTimeBasedEventDeserializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.LabelTag;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "Time-Based Calendar Event", nip = 52)
@JsonDeserialize(using = CalendarTimeBasedEventDeserializer.class)
@NoArgsConstructor
public class CalendarTimeBasedEvent extends CalendarDateBasedEvent {

    public CalendarTimeBasedEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content) {
        super(sender, baseTags, content);
        this.setKind(Kind.CALENDAR_TIME_BASED_EVENT.getValue());
    }

    public String getStartTzid() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getStartTzid();
    }

    public String getEndTzid() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getEndTzid();
    }

    public String getSummary() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getSummary();
    }

    public String getLocation() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getLocation();
    }

    public List<String> getLabels() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getLabelTags() != null ? calendarContent.getLabelTags().stream().map(l -> "#" + l.getNameSpace() + "." + l.getLabel()).toList() : null;
    }

    @Override
    protected CalendarContent getCalendarContent() {

        CalendarContent calendarContent = super.getCalendarContent();

        GenericTag start_tzidTag = getTag("start_tzid");
        GenericTag end_tzidTag = getTag("end_tzid");
        GenericTag summaryTag = getTag("summary");
        GenericTag locationTag = getTag("location");
        List<GenericTag> lTags = getTags("l");

        // Update the calendarContent object with the values from the tags
        if (start_tzidTag != null) {
            calendarContent.setStartTzid(start_tzidTag.getAttributes().get(0).getValue().toString());
        }

        if (end_tzidTag != null) {
            calendarContent.setEndTzid(end_tzidTag.getAttributes().get(0).getValue().toString());
        }

        if (summaryTag != null) {
            calendarContent.setSummary(summaryTag.getAttributes().get(0).getValue().toString());
        }

        if (locationTag != null) {
            calendarContent.setLocation(locationTag.getAttributes().get(0).getValue().toString());
        }

        if (lTags != null) {
            for (GenericTag lTag : lTags) {
                calendarContent.addLabelTag(GenericTag.convert(lTag, LabelTag.class));
            }
        }

        return calendarContent;
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        GenericTag dTag = getTag("d");
        if (dTag == null) {
            throw new AssertionError("Missing \\`d\\` tag for the event identifier.");
        }

        GenericTag titleTag = getTag("title");
        if (titleTag == null) {
            throw new AssertionError("Missing \\`title\\` tag for the event title.");
        }

        GenericTag startTag = getTag("start");
        if (startTag == null) {
            throw new AssertionError("Missing \\`start\\` tag for the event start.");
        }

        try {
            Long.parseLong(startTag.getAttributes().get(0).getValue().toString());
        } catch (NumberFormatException e) {
            throw new AssertionError("Invalid \\`start\\` tag value: must be a numeric timestamp.");
        }
    }
}
