
package nostr.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author squirrel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericTagQuery {

  private String tagName;

  @JsonProperty
  private String value;

  @JsonIgnore
  public Integer getNip() {
    return 1;
  }
}
