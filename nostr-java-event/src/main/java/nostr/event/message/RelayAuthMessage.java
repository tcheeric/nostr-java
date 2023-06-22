package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class RelayAuthMessage extends BaseAuthMessage {

    @JsonProperty
    private final String challenge;

    public RelayAuthMessage(String challenge) {
        super(Command.AUTH.name());
        this.challenge = challenge;
    }

}
