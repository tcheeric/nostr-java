package nostr.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.base.IElement;

import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
@Data
@NoArgsConstructor
public class Nip05Content implements IElement {

  @JsonProperty("names")
  private Map<String, String> names;

  @JsonProperty("relays")
  private Map<String, List<String>> relays;
}
