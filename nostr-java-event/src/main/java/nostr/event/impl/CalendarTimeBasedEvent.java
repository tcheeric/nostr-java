package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.json.deserializer.CalendarTimeBasedEventDeserializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.LabelTag;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = false)
@Event(name = "Time-Based Calendar Event", nip = 52)
@JsonDeserialize(using = CalendarTimeBasedEventDeserializer.class)
@NoArgsConstructor
public class CalendarTimeBasedEvent<T extends BaseTag> extends CalendarDateBasedEvent<T> {

  public CalendarTimeBasedEvent(
      @NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content) {
    super(sender, baseTags, content);
    this.setKind(Kind.CALENDAR_TIME_BASED_EVENT.getValue());
  }

  public Optional<String> getStartTzid() {
    return getCalendarContent().getStartTzid();
  }

  public Optional<String> getEndTzid() {
    return getCalendarContent().getEndTzid();
  }

  public Optional<String> getSummary() {
    return getCalendarContent().getSummary();
  }

  public Optional<String> getLocation() {
    return super.getLocation();
  }

  public List<String> getLabels() {
    List<LabelTag> labelTags = getCalendarContent().getLabelTags();
    return labelTags.stream().map(l -> "#" + l.getNameSpace() + "." + l.getLabel()).toList();
  }

  @Override
  protected CalendarContent<T> getCalendarContent() {
    CalendarContent<T> calendarContent = super.getCalendarContent();

    // Update the calendarContent object with the values from the tags
    calendarContent.setStartTzid(
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "start_tzid", this)
            .getAttributes()
            .get(0)
            .value()
            .toString());
    calendarContent.setEndTzid(
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "end_tzid", this)
            .getAttributes()
            .get(0)
            .value()
            .toString());
    calendarContent.setSummary(
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "summary", this)
            .getAttributes()
            .get(0)
            .value()
            .toString());
    calendarContent.setLocation(
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "location", this)
            .getAttributes()
            .get(0)
            .value()
            .toString());
    nostr.event.filter.Filterable
        .getTypeSpecificTags(LabelTag.class, this)
        .forEach(calendarContent::addLabelTag);

    return calendarContent;
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.CALENDAR_TIME_BASED_EVENT.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.CALENDAR_TIME_BASED_EVENT.getValue());
    }
  }
}
