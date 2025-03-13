package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Key
    @JsonProperty
    private String currency;

    @Key
    @JsonProperty
    private String frequency;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        String text = Optional.ofNullable(node.get(1)).orElseThrow().asText();
        final BigDecimal number = new BigDecimal(text);
        final String currency = Optional.ofNullable(node.get(2)).orElseThrow().asText();

        PriceTag tag = PriceTag.builder().number(number).currency(currency).build();
        Optional.ofNullable(node.get(3)).ifPresent(jsonNode1 -> tag.setFrequency(jsonNode1.asText()));

        return (T) tag;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) &&
                Objects.equals(
                        number.stripTrailingZeros(),
                        ((PriceTag) o).number.stripTrailingZeros()
                )
                && Objects.equals(currency, ((PriceTag) o).currency) && Objects.equals(frequency, ((PriceTag) o).frequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), number.stripTrailingZeros(), currency, frequency);
    }
}
