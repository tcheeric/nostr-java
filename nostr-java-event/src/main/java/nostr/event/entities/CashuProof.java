package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.event.json.codec.EventEncodingException;

import static nostr.base.json.EventJsonMapper.mapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CashuProof {

  @EqualsAndHashCode.Include private String id;
  private Integer amount;

  @EqualsAndHashCode.Include private String secret;

  @JsonProperty("C")
  @EqualsAndHashCode.Include
  private String C;

  @EqualsAndHashCode.Include
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String witness;

  @Override
  public String toString() {
    try {
      return mapper().writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to serialize Cashu proof", ex);
    }
  }
}
