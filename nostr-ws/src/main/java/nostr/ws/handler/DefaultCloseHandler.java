
package nostr.ws.handler;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class DefaultCloseHandler extends BaseHandler {

    private final int statusCode;
    private final String reason;

}
