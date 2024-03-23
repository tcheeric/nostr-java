package nostr.event.tag;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.PriceTagSerializer;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "price", nip = 99)
@RequiredArgsConstructor
@JsonSerialize(using = PriceTagSerializer.class)
public class PriceTag extends BaseTag {
  private final String price;
  private final String number;
  private final String currency;
  private final String frequency;
}
