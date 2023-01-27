
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
public class NoticeMessage extends GenericMessage {

    private final String message;

    public NoticeMessage(String message) {
        super(Command.NOTICE.name());
        this.message = message;
    }
}
