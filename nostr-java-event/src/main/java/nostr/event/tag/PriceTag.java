package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.impl.GenericTag;
import nostr.event.json.serializer.PriceTagSerializer;

import java.math.BigDecimal;

@JsonPropertyOrder({"number", "currency", "frequency"})
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "price", nip = 99)
@JsonSerialize(using = PriceTagSerializer.class)
public class PriceTag extends GenericTag {
  @Key
  @JsonProperty
  private final BigDecimal number;

  @Key
  @JsonProperty
  private final String currency;

  @Key
  @JsonProperty
  private final String frequency;

  public PriceTag(BigDecimal number, String currency, String frequency) {
    super("price", 99);
    this.number = number;
    this.currency = currency;
    this.frequency = frequency;
  }
}
