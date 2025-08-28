package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author squirrel
 */
public record ElementAttribute(
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) String name,
    @JsonProperty Object value) {}
