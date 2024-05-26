package nostr.event.tag;

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
}