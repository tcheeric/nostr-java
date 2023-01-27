
package nostr.event;

import nostr.base.IElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Data
@AllArgsConstructor
@ToString
@Deprecated
public abstract class BaseMessage implements IElement {

    private final String command;
}
