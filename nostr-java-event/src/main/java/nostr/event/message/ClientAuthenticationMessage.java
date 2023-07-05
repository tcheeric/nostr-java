
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.ClientAuthenticationEvent;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ClientAuthenticationMessage extends BaseAuthMessage {

    @JsonProperty
    private final ClientAuthenticationEvent event;
    
    public ClientAuthenticationMessage(ClientAuthenticationEvent event) {
        super(Command.AUTH.name());
        this.event = event;
    }
    
}