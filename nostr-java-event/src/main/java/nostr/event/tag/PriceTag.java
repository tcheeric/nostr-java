package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

import java.math.BigDecimal;
import java.util.Optional;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "price", nip = 99)
@RequiredArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"number", "currency", "frequency"})
public class PriceTag extends BaseTag {
  @JsonProperty
  private final BigDecimal number;

  @JsonProperty
  private final String currency;

  @JsonProperty
  private String frequency;

  public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
    final JsonNode number = Optional.ofNullable(node.get(1)).orElseThrow();
    final JsonNode currency = Optional.ofNullable(node.get(2)).orElseThrow();

    PriceTag tag = new PriceTag(
        new BigDecimal(number.bigIntegerValue()),
        currency.asText());
    Optional.ofNullable(node.get(3)).ifPresent(jsonNode1 -> tag.setFrequency(jsonNode1.asText()));

    return (T) tag;
  }
}
