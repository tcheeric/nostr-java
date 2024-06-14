package nostr.event.tag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.Relay;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.RelaysTagSerializer;

import java.util.List;
import java.util.Optional;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "relays", nip = 57)
@JsonSerialize(using = RelaysTagSerializer.class)
public class RelaysTag extends BaseTag {
  private final List<Relay> relays;

  public RelaysTag(@NonNull List<Relay> relays) {
    this.relays = relays;
  }

  public RelaysTag(@NonNull Relay... relays) {
    this(List.of(relays));
  }

  public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
    return (T) new RelaysTag(Optional.of(node).stream().map(jsonNode -> new Relay(jsonNode.get(1).asText())).toList());
  }
}