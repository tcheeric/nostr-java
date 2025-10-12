package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Marker;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/** Represents an 'e' event reference tag (NIP-01). */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "e", name = "event")
@JsonPropertyOrder({"idEvent", "recommendedRelayUrl", "marker"})
@NoArgsConstructor
@AllArgsConstructor
public class EventTag extends BaseTag {

  @Key @JsonProperty private String idEvent;

  @Key
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String recommendedRelayUrl;

  @Key(nip = 10)
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Marker marker;

  public EventTag(String idEvent) {
    this.idEvent = idEvent;
  }

  /** Optional accessor for recommendedRelayUrl. */
  public Optional<String> getRecommendedRelayUrlOptional() {
    return Optional.ofNullable(recommendedRelayUrl);
  }

  /** Optional accessor for marker. */
  public Optional<Marker> getMarkerOptional() {
    return Optional.ofNullable(marker);
  }

  public static EventTag deserialize(@NonNull JsonNode node) {
    EventTag tag = new EventTag();
    setRequiredField(node.get(1), (n, t) -> tag.setIdEvent(n.asText()), tag);
    setOptionalField(node.get(2), (n, t) -> tag.setRecommendedRelayUrl(n.asText()), tag);
    setOptionalField(
        node.get(3), (n, t) -> tag.setMarker(Marker.valueOf(n.asText().toUpperCase())), tag);
    return tag;
  }

  public static EventTag updateFields(@NonNull GenericTag tag) {
    if (!"e".equals(tag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for EventTag");
    }
    EventTag eventTag = new EventTag(tag.getAttributes().get(0).value().toString());
    if (tag.getAttributes().size() > 1) {
      eventTag.setRecommendedRelayUrl(tag.getAttributes().get(1).value().toString());
    }
    if (tag.getAttributes().size() > 2) {
      eventTag.setMarker(
          Marker.valueOf(tag.getAttributes().get(2).value().toString().toUpperCase()));
    }

    return eventTag;
  }
}
