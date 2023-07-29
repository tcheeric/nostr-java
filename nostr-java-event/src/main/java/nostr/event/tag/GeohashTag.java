package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/**
 *
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "g", nip = 12)
@NoArgsConstructor
@AllArgsConstructor
public class GeohashTag extends BaseTag {
    
    @Key
    @JsonProperty("g")
    private String location;
}
