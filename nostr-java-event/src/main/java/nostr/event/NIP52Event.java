package nostr.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.GenericEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class NIP52Event extends GenericEvent {
  @Getter
  @JsonIgnore
  private CalendarContent calendarContent;

  public NIP52Event(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(pubKey, kind.getValue(), baseTags, content);
    this.calendarContent = calendarContent;
    mapCustomTags();
  }

  private void mapCustomTags() {
    addStandardTag(calendarContent.getIdentifierTag());
    addGenericTag("title", getNip(), calendarContent.getTitle());
    addGenericTag("start", getNip(), calendarContent.getStart());
    addGenericTag("end", getNip(), calendarContent.getEnd());
    addGenericTag("start_tzid", getNip(), calendarContent.getStartTzid());
    addGenericTag("end_tzid", getNip(), calendarContent.getEndTzid());
    addGenericTag("summary", getNip(), calendarContent.getSummary());
    addGenericTag("image", getNip(), calendarContent.getImage());
    addGenericTag("location", getNip(), calendarContent.getLocation());
    addStandardTag(calendarContent.getGeohashTag());
    addStandardTag(calendarContent.getParticipantPubKeys());
    addStringListTag("l", getNip(), calendarContent.getLabels());
    addStandardTag(calendarContent.getHashtagTags());
    addStandardTag(calendarContent.getReferenceTags());
  }
}
