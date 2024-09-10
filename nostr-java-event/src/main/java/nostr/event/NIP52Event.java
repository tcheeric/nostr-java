package nostr.event;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class NIP52Event extends GenericEvent {
  private CalendarContent calendarContent;

  public NIP52Event(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull CalendarContent calendarContent) {
    super(pubKey, kind.getValue(), baseTags, content);
    this.calendarContent = calendarContent;
    appendTags();
  }

  private void appendTags() {
    addStandardTag(calendarContent.getIdentifierTag());
    addGenericTag("title", calendarContent.getTitle());
    addGenericTag("start", calendarContent.getStart());
    addGenericTag("end", calendarContent.getEnd());
    addGenericTag("start_tzid", calendarContent.getStartTzid());
    addGenericTag("end_tzid", calendarContent.getEndTzid());
    addGenericTag("summary", calendarContent.getSummary());
    addGenericTag("image", calendarContent.getImage());
    addGenericTag("location", calendarContent.getLocation());
    addStandardTag(calendarContent.getGeohashTag());
    addStandardTag(calendarContent.getParticipantPubKeys());
    addStringListTag("l", calendarContent.getLabels());
    addStandardTag(calendarContent.getHashtagTags());
    addStandardTag(calendarContent.getReferenceTags());
  }

  // TODO: possibly refactor below into parent/hierarchy, take a look at Product/Classifieds first.
  private <T extends BaseTag> void addStandardTag(List<T> tag) {
    Optional.ofNullable(tag).ifPresent(tagList -> tagList.forEach(this::addStandardTag));
  }

  private void addStandardTag(BaseTag tag) {
    Optional.ofNullable(tag).ifPresent(this::addTag);
  }

  private void addGenericTag(String key, Object value) {
    Optional.ofNullable(value).ifPresent(s -> addTag(GenericTag.create(key, 52, s.toString())));
  }

  private void addStringListTag(String label, List<String> tag) {
    Optional.ofNullable(tag).ifPresent(tagList -> addGenericTag(label, tagList));
  }
}
