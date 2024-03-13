package nostr.ws.request.handler.provider;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.base.annotation.DefaultHandler;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;
import nostr.ws.Connection;
import nostr.ws.handler.spi.IRequestHandler;

/**
 *
 * @author squirrel
 */
@Log
@Data
@DefaultHandler
public class DefaultRequestHandler implements IRequestHandler {

    private Connection connection;
    private List<BaseMessage> responses;
    
    public DefaultRequestHandler(@NonNull List<BaseMessage> responses){
    	this.responses = responses;
    }

    @Override
    public void process(BaseMessage message, Relay relay) throws NostrException {
        try {
            this.connection = new Connection(relay, responses);
            sendMessage(message);
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private void sendMessage(BaseMessage message) throws IOException, NostrException {

        final Relay relay = connection.getRelay();

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
