
package com.tcheeric.nostr.controller.handler.response;

import com.tcheeric.nostr.base.Command;
import com.tcheeric.nostr.base.NostrException;
import java.io.IOException;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Log
public class EventResponseHandler extends BaseResponseHandler {
    
    private final String subscriptionId;
    private final String jsonEvent;

    public EventResponseHandler(String subscriptionId, String jsonEvent) {
        super(Command.EVENT);
        this.subscriptionId = subscriptionId;
        this.jsonEvent = jsonEvent;
    }

    @Override
    public void process() throws IOException, NostrException {
        log.log(Level.FINE, ">>>>>>>>>>>>>> {0}", this);
    }
}
