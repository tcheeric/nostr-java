package nostr.ws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.util.NostrException;
import nostr.ws.handler.spi.IResponseHandler;
import nostr.ws.response.handler.provider.ResponseHandlerImpl;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 *
 * @author squirrel
 */
@WebSocket(idleTimeout = Integer.MAX_VALUE)
@Log
public class ClientListenerEndPoint {

    private final IResponseHandler responseHandler;

    public ClientListenerEndPoint() {
        this.responseHandler = new ResponseHandlerImpl();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        log.log(Level.FINE, "onConnect Relay {0}", session.getRemoteAddress());

        session.setMaxTextMessageSize(16 * 1024);

        log.log(Level.INFO, "Connected");
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        log.log(Level.FINE, "onClose");

        disposeResources();

        log.log(Level.INFO, "Connection closed with parameters: Reason {0} - StatusCode: {1}", new Object[]{statusCode, reason});
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        log.fine("onError");

        disposeResources();

        log.log(Level.SEVERE, "An error has occurred: {0}", cause);
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, @NonNull String message) throws IOException, NostrException {

        if ("close".equalsIgnoreCase(message)) {
            session.close(StatusCode.NORMAL, "bye");
            return;
        }
        
        responseHandler.process(message, getRelay(session));
    }

    @OnWebSocketMessage
    public void onBinaryMessage(byte[] payload, int offset, int length) {
        log.fine("onBinaryMessage");

        // Save only PNG images.
        byte[] pngBytes = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        for (int i = 0; i < pngBytes.length; ++i) {
            if (pngBytes[i] != payload[offset + i]) {
                return;
            }
        }
        savePNGImage(payload, offset, length);
    }

    private Relay getRelay(Session session) {
        SocketAddress remoteAddress = session.getRemoteAddress();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
        String remoteHostname = inetSocketAddress.getHostName();
        return Relay.builder().uri(remoteHostname).build();
    }

    private void disposeResources() {
        log.log(Level.FINE, "disposeResources");
    }

    private void savePNGImage(byte[] payload, int offset, int length) {
        log.log(Level.FINE, "savePNGImage");
    }
}
