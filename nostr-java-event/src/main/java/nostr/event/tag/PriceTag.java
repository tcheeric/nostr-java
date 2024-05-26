package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "price", nip = 99)
@RequiredArgsConstructor
@JsonPropertyOrder({"number", "currency", "frequency"})
//@JsonSerialize(using = PriceTagSerializer.class)
public class PriceTag extends BaseTag {
  @JsonProperty
  private final BigDecimal number;
  @JsonProperty
  private final String currency;
  @JsonProperty
  private final String frequency;
}
