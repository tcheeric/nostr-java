
package nostr.event.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class AuthMessage extends GenericMessage {

    private final String challenge;
    
    public AuthMessage(String challenge) {
        super(Command.AUTH.name());
        this.challenge = challenge;
    }
    
}
