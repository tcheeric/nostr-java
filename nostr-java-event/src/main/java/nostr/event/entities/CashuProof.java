package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CashuProof {

    @EqualsAndHashCode.Include
    private String id;
    private Integer amount;

    @EqualsAndHashCode.Include
    private String secret;

    @JsonProperty("C")
    @EqualsAndHashCode.Include
    private String C;

    @EqualsAndHashCode.Include
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String witness;

    @SneakyThrows
    @Override
    public String toString() {
        return MAPPER_BLACKBIRD.writeValueAsString(this);
    }
}
