
package nostr.ws.handler.request;

import nostr.ws.Connection;
import nostr.event.BaseMessage;
import nostr.event.marshaller.impl.MessageMarshaller;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.IHandler;
import nostr.util.NostrException;
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

    private final BaseMessage message;
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
        final Session session = this.connection.getSession();
        if (session != null) {
            RemoteEndpoint remote = session.getRemote();
            log.log(Level.FINE, "Sending Message: {0}", message);
            final String msg = new MessageMarshaller(message, connection.getRelay()).marshall();
            remote.sendString(msg);
            return;
        }
        throw new NostrException("Could not get a session");
    }

}
