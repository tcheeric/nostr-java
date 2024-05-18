package nostr.event.tag;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.PriceTagSerializer;

import java.util.List;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "relays", nip = 57)
@JsonSerialize(using = PriceTagSerializer.class)
public class RelaysTag extends BaseTag {
  private final List<String> relayUris;

  public RelaysTag(List<String> relayUris) {
    this.relayUris = relayUris;
  }
}
