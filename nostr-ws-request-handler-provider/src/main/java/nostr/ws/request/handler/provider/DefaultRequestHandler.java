package nostr.ws.request.handler.provider;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.base.annotation.DefaultHandler;
import nostr.event.impl.GenericMessage;
import nostr.event.marshaller.impl.MessageMarshaller;
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
@NoArgsConstructor
@DefaultHandler
public class DefaultRequestHandler implements IRequestHandler {

    //private GenericMessage message;
    private Connection connection;

    @Override
    public void process(GenericMessage message, Relay relay) throws NostrException {
        try {
            this.connection = new Connection(relay);
            sendMessage(message);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        } finally {
            //this.connection.stop();
        }
    }

    private void sendMessage(GenericMessage message) throws IOException, NostrException {

        final Relay relay = connection.getRelay();

        if (!relay.getSupportedNips().contains(message.getNip())) {
            throw new UnsupportedNIPException(String.format("NIP-%d is not supported by relay %s. Supported NIPS: %s", new Object[]{message.getNip(), relay, relay.printSupportedNips()}));
        }

        final Session session = this.connection.getSession();
        if (session != null) {
            RemoteEndpoint remote = session.getRemote();

            final String msg = new MessageMarshaller(message, relay).marshall();

            log.log(Level.INFO, ">>> Sending Message: {0}", msg);

            remote.sendString(msg);

            return;
        }
        throw new NostrException("Could not get a session");
    }

}
