package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.AbstractEventContent;
import nostr.event.NIP52Event;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;

import java.util.List;

@Setter
@Getter
public class CalendarContent extends AbstractEventContent<NIP52Event> {
  @JsonIgnore
  private String id;

  // below fields required
  @JsonProperty("d")
  private final IdentifierTag identifierTag;

  @JsonProperty
  private final String title;

  @JsonProperty
  private final Long start;

  // below fields optional
  @JsonProperty
  private Long end;

  @JsonProperty("start_tzid")
  private String startTzid;

  @JsonProperty("end_tzid")
  private String endTzid;

  @JsonProperty
  private String summary;

  @JsonProperty
  private String image;

  @JsonProperty
  private String location;

  @JsonProperty("g")
  private GeohashTag geohashTag;

  @JsonProperty("p")
  private List<PubKeyTag> participantPubKeys;

  @JsonProperty("l")
  private List<String> labels;

  @JsonProperty("t")
  private List<HashtagTag> hashtagTags;

  @JsonProperty("r")
  private List<ReferenceTag> referenceTags;

  public CalendarContent(@NonNull IdentifierTag identifierTag, @NonNull String title, @NonNull Long start) {
    this.identifierTag = identifierTag;
    this.title = title;
    this.start = start;
  }

  public CalendarContent(@NonNull String uuid, @NonNull String title, @NonNull Long start) {
    this.identifierTag = new IdentifierTag(uuid);
    this.title = title;
    this.start = start;
  }
}