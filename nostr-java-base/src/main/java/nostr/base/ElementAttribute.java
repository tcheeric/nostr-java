package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class ElementAttribute {

    @JsonProperty    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @EqualsAndHashCode.Include
    @NonNull
    private final String name;
    
    @JsonProperty
    @EqualsAndHashCode.Include
    private final Object value;
}
