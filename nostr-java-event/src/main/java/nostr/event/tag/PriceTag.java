package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Tag(code = "price", nip = 99)
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"number", "currency", "frequency"})
public class PriceTag extends BaseTag {
  @JsonProperty
  private BigDecimal number;
  @JsonProperty
  private String currency;
  @JsonProperty
  private String frequency;
}
