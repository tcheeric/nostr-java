package nostr.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 * @author guilhermegps
 *
 */
@Builder
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class ContentReason {

    private final String reason;

    @Override
    public String toString() {
        try {
            return MAPPER_AFTERBURNER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
