package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/** Represents a 'price' tag (NIP-99). */
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

  @Key
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String frequency;

  /** Optional accessor for frequency. */
  public Optional<String> getFrequencyOptional() {
    return Optional.ofNullable(frequency);
  }

  public static PriceTag deserialize(@NonNull JsonNode node) {
    PriceTag tag = new PriceTag();
    setRequiredField(node.get(1), (n, t) -> tag.setNumber(new BigDecimal(n.asText())), tag);
    setOptionalField(node.get(2), (n, t) -> tag.setCurrency(n.asText()), tag);
    setOptionalField(node.get(3), (n, t) -> tag.setFrequency(n.asText()), tag);
    return tag;
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
    BigDecimal number = new BigDecimal(genericTag.getAttributes().get(0).value().toString());
    String currency = genericTag.getAttributes().get(1).value().toString();
    String frequency =
        genericTag.getAttributes().size() > 2
            ? genericTag.getAttributes().get(2).value().toString()
            : null;
    return new PriceTag(number, currency, frequency);
  }
}
