package nostr.ws;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.context.CommandContext;
import nostr.context.Context;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultCommandContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.controller.CommandController;
import nostr.controller.command.CommandControllerImpl;
import nostr.event.BaseMessage;
import nostr.event.Response;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.ClosedMessage;
import nostr.event.message.RelayAuthenticationMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author squirrel
 */
@WebSocket(idleTimeout = Integer.MAX_VALUE)
@Log
public class RelayClientListenerEndPoint {

    private RequestContext requestContext;

    @Getter
    private final List<Session> activeSessions;

    @Getter
    private Set<Response> responses;

    private static RelayClientListenerEndPoint INSTANCE;

    public RelayClientListenerEndPoint(@NonNull RequestContext requestContext) {
        this.requestContext = requestContext;
        this.activeSessions = new ArrayList<>();
        this.responses = new HashSet<>();
    }

    public static RelayClientListenerEndPoint getInstance(@NonNull RequestContext context) {
        INSTANCE = INSTANCE == null ? new RelayClientListenerEndPoint(context) : INSTANCE;
        return INSTANCE;
    }

    @OnWebSocketConnect
    public void onConnect(@NonNull Session session) {
        session.setMaxTextMessageSize(16 * 1024);
        log.log(Level.INFO, "Add active session {0} -> {1}", new Object[]{session.getLocalAddress(), session.getRemoteAddress()});
        this.activeSessions.add(session);

        log.log(Level.INFO, "Connected to relay {0}", session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason, Session session) {
        log.log(Level.FINER, "onClose");

        disposeResources(session);

        log.log(Level.WARNING, "Connection closed with parameters: Reason {0} - StatusCode: {1} - Relay {2}", new Object[]{reason, statusCode, getRelay(session).toString()});
    }

    @OnWebSocketError
    public void onError(Throwable cause, Session session) {
        log.log(Level.SEVERE, "An error has occurred: {0} (Relay: {1})", new Object[]{cause.getMessage(), getRelay(session).toString()});

        disposeResources(session);

        session.close(StatusCode.SERVER_ERROR, cause.getMessage());
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, @NonNull String message) {

        if ("close".equalsIgnoreCase(message)) {
            session.close(StatusCode.NORMAL, "bye");
            return;
        }

        log.log(Level.INFO, "Received message: {0} from relay {1}", new Object[]{message, getRelay(session).toString()});

        var msg = new BaseMessageDecoder(message).decode();
        final String strCommand = msg.getCommand();

        log.log(Level.INFO, "Creating the command context with message {0} and session {1}", new Object[]{msg, session});
        Context context = createCommandContext(msg, session);
        CommandController commandController = new CommandControllerImpl(strCommand);

        commandController.handleRequest(context);
        List<Response> responses = ((CommandControllerImpl) commandController).getResponses();
        this.responses.addAll(responses);
        log.info("Done! Responses: " + this.responses.size());
    }

    @OnWebSocketMessage
    public void onBinaryMessage(byte[] payload, int offset, int length) {

        // Save only PNG images.
        byte[] pngBytes = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        for (int i = 0; i < pngBytes.length; ++i) {
            if (pngBytes[i] != payload[offset + i]) {
                return;
            }
        }
        savePNGImage(payload, offset, length);
    }

    public boolean isConnected(@NonNull Relay relay) {
        var session = this.activeSessions.stream().filter(s -> getRelay(s).equals(relay)).findFirst();
        if (session.isPresent()) {
            return session.get().isOpen();
        } else {
            return false;
        }
    }

    public Session getSession(@NonNull Relay relay) {
        return this.activeSessions.stream().filter(s -> getRelay(s).equals(relay)).findFirst().orElse(null);
    }

    private Relay getRelay(@NonNull Session session) {
        var remoteAddress = session.getRemoteAddress();
        var inetSocketAddress = (InetSocketAddress) remoteAddress;
        var remoteHostname = inetSocketAddress.getHostName();
        var port = inetSocketAddress.getPort();

        if (session.isSecure()) {
            return new Relay(Relay.PROTOCOL_WSS, remoteHostname, port);
        } else {
            return new Relay(Relay.PROTOCOL_WS, remoteHostname, port);
        }
    }

    private void disposeResources(@NonNull Session session) {
        log.log(Level.FINE, "disposeResources");

        this.activeSessions.remove(session);
        log.log(Level.INFO, "Session removed: {0} -> {1} - Relay: {2}", new Object[]{session.getLocalAddress(), session.getRemoteAddress(), getRelay(session).toString()});
    }

    private CommandContext createCommandContext(@NonNull BaseMessage message, Session session) {
        var commandContext = new DefaultCommandContext();

        // Pass on the message
        commandContext.setMessage(message);

        // Pass on the private key
        commandContext.setPrivateKey(((DefaultRequestContext) this.requestContext).getPrivateKey());

        // Pass on the first relay
        var relay = getRelay(session);
        commandContext.setRelay(relay);

        // Set the challenge
        if (message instanceof RelayAuthenticationMessage authMessage) {
            ((DefaultRequestContext) this.requestContext).setChallenge(relay, authMessage.getChallenge());
        }

        if (message instanceof ClosedMessage closedMessage) {
            commandContext.setChallenge(((DefaultRequestContext) this.requestContext).getChallenge(relay));
        }

        return commandContext;
    }

    private void savePNGImage(byte[] payload, int offset, int length) {
        log.log(Level.FINEST, "savePNGImage");
    }

    private void printSessions() {
        log.log(Level.FINEST, "printStatus");
        log.log(Level.INFO, "Active Sessions: {0}", this.activeSessions.size());
        activeSessions.forEach(s -> log.log(Level.INFO, "\tSession: {1} -> {0} ({2})", new Object[]{s.getRemoteAddress(), s.getLocalAddress(), s.isOpen()}));
    }
}
