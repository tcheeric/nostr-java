package nostr.ws.handler.request;

import nostr.ws.Connection;
import nostr.event.marshaller.impl.MessageMarshaller;
import java.io.IOException;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.IHandler;
import nostr.base.Relay;
import nostr.event.impl.GenericMessage;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

/**
 *
 * @author squirrel
 */
@Builder
@Log
@Data
@AllArgsConstructor
public class RequestHandler implements IHandler {

    private final GenericMessage message;
    private final Connection connection;

    @Override
    public void process() throws NostrException {
        try {
            sendMessage();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }
    }

    private void sendMessage() throws IOException, NostrException {
        
        final Relay relay = connection.getRelay();

        if (!relay.getSupportedNips().contains(message.getNip())) {
            throw new UnsupportedNIPException(String.format("NIP-%d is not supported by relay %s. Supported NIPS: %s", new Object[]{message.getNip(), relay, relay.printSupportedNips()}));
        }

        final Session session = this.connection.getSession();
        if (session != null) {
            RemoteEndpoint remote = session.getRemote();

            log.log(Level.FINE, "Sending Message: {0}", message);

            final String msg = new MessageMarshaller(message, relay).marshall();
            remote.sendString(msg);

            return;
        }
        throw new NostrException("Could not get a session");
    }

}
