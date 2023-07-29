package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.ExpirationTagSerializer;

/**
 *
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Tag(code = "expiration", name = "Expiration Timestamp", nip = 40)
@NoArgsConstructor
@JsonSerialize(using = ExpirationTagSerializer.class)
public class ExpirationTag extends BaseTag {

    @Key
    @JsonProperty
    private Integer expiration;    
}
