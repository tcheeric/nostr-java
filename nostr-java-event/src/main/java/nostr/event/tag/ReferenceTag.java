package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.ReferenceTagSerializer;

/**
 *
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "r", nip = 12)
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = ReferenceTagSerializer.class)
public class ReferenceTag extends BaseTag {

    @Key
    @JsonProperty("uri")
    private URI uri;

}
