package nostr.util.validator;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author eric
 */
@Data
@NoArgsConstructor
public class Nip05Content  {

    @JsonProperty("names")
    private Map<String, String> names;

    @JsonProperty("relays")
    private Map<String, List<String>> relays;
}
