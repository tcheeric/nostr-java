package nostr.ws.request.handler.provider;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.DefaultHandler;
import nostr.base.context.RequestContext;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;
import nostr.ws.Connection;
import nostr.ws.handler.spi.ApplicationController;
import nostr.ws.request.DefaultRequestContext;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * @author squirrel
 */
@Log
@Data
@DefaultHandler
public class DefaultApplicationController implements ApplicationController {

    private Connection connection;
    private List<BaseMessage> responses;

    public DefaultApplicationController(@NonNull List<BaseMessage> responses) {
        this.responses = responses;
    }

    @Override
    public void execute(@NonNull RequestContext context) throws NostrException {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            try {
                var relay = defaultRequestContext.getRelay();
                this.connection = new Connection(relay, responses);
                sendMessage(context);
            } catch (Exception ex) {
                throw new NostrException(ex);
            }
        }
    }

    private void sendMessage(@NonNull RequestContext context) throws IOException, NostrException {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            var relay = defaultRequestContext.getRelay();
            var message = defaultRequestContext.getMessage();

            if (!relay.getSupportedNips().contains(message.getNip())) {
                throw new UnsupportedNIPException(String.format("NIP-%d is not supported by relay %s. Supported NIPS: %s", message.getNip(), relay, relay.printSupportedNips()));
            }

            final Session session = this.connection.getSession();
            if (session != null) {
                RemoteEndpoint remote = session.getRemote();

                final String msg = new BaseMessageEncoder(message, relay).encode();

                log.log(Level.INFO, "Sending Message: {0}", msg);
                remote.sendString(msg);
                return;
            }
            throw new NostrException("Could not get a session");

        }
    }

}
