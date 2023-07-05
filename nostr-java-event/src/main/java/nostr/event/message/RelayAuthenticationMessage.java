package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class RelayAuthenticationMessage extends BaseAuthMessage {

    @JsonProperty
    private final String challenge;

    public RelayAuthenticationMessage(String challenge) {
        super(Command.AUTH.name());
        this.challenge = challenge;
    }

}
