
package nostr.ws.handler.response;

import nostr.base.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.IHandler;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@AllArgsConstructor
public abstract class BaseResponseHandler implements IHandler {
    
    private final Command command;
    
}
