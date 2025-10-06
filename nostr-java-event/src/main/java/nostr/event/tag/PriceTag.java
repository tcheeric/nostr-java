package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

@Builder
@Data
@Tag(code = "price", nip = 99)
@RequiredArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"number", "currency", "frequency"})
public class PriceTag extends BaseTag {

  @Key
  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal number;

  @Key @JsonProperty private String currency;

  @Key @JsonProperty private String frequency;

  @SuppressWarnings("unchecked")
  public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
    PriceTag tag = new PriceTag();
    setRequiredField(node.get(1), (n, t) -> tag.setNumber(new BigDecimal(n.asText())), tag);
    setOptionalField(node.get(2), (n, t) -> tag.setCurrency(n.asText()), tag);
    setOptionalField(node.get(3), (n, t) -> tag.setFrequency(n.asText()), tag);
    return (T) tag;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    PriceTag priceTag = (PriceTag) o;
    return Objects.equals(number.stripTrailingZeros(), priceTag.number.stripTrailingZeros())
        && Objects.equals(currency, priceTag.currency)
        && Objects.equals(frequency, priceTag.frequency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), number.stripTrailingZeros(), currency, frequency);
  }

  public static PriceTag updateFields(@NonNull GenericTag genericTag) {
    if (!"price".equals(genericTag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for PriceTag");
    }

    if (genericTag.getAttributes().size() < 2 || genericTag.getAttributes().size() > 3) {
      throw new IllegalArgumentException("Invalid number of attributes for PriceTag");
    }

    PriceTag tag = new PriceTag();
    tag.setNumber(new BigDecimal(genericTag.getAttributes().get(0).value().toString()));
    tag.setCurrency(genericTag.getAttributes().get(1).value().toString());

    if (genericTag.getAttributes().size() > 2) {
      tag.setFrequency(genericTag.getAttributes().get(2).value().toString());
    }
    return tag;
  }
}
