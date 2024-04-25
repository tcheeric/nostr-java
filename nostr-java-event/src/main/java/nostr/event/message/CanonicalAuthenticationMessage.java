
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.CanonicalAuthenticationEvent;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class CanonicalAuthenticationMessage extends BaseAuthMessage {

    @JsonProperty
    private final CanonicalAuthenticationEvent event;
    
    public CanonicalAuthenticationMessage(CanonicalAuthenticationEvent event) {
        super(Command.AUTH.name());
        this.event = event;
    }
    
}