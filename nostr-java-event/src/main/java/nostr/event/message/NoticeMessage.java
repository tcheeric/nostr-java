
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class NoticeMessage extends GenericMessage {

    @JsonProperty
    private final String message;

    public NoticeMessage(String message) {
        super(Command.NOTICE.name());
        this.message = message;
    }
}
