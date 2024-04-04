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
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.OkMessage;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author squirrel
 */
@WebSocket(idleTimeout = Integer.MAX_VALUE)
@Log
public class ClientListenerEndPoint {

    private RequestContext context;

    @Getter
    private final List<Session> activeSessions;

    private static ClientListenerEndPoint INSTANCE;

    private ClientListenerEndPoint(@NonNull RequestContext context) {
        this.context = context;
        this.activeSessions = new ArrayList<>();
    }

    public static ClientListenerEndPoint getInstance(@NonNull RequestContext context) {
        INSTANCE = INSTANCE == null ? new ClientListenerEndPoint(context) : INSTANCE;
        return INSTANCE;
    }

    @OnWebSocketConnect
    public void onConnect(@NonNull Session session) {
        session.setMaxTextMessageSize(16 * 1024);
        this.activeSessions.add(session);
        log.log(Level.INFO, "Connected to relay {0}", session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason, Session session) {
        log.log(Level.FINER, "onClose");

        disposeResources(session);

        log.log(Level.WARNING, "Connection closed with parameters: Reason {0} - StatusCode: {1}", new Object[]{reason, statusCode});
    }

    @OnWebSocketError
    public void onError(Throwable cause, Session session) {
        log.log(Level.SEVERE, "An error has occurred: {0}", cause.getMessage());

        disposeResources(session);

        session.close(StatusCode.SERVER_ERROR, cause.getMessage());
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, @NonNull String message) {

        if ("close".equalsIgnoreCase(message)) {
            session.close(StatusCode.NORMAL, "bye");
            return;
        }

        log.log(Level.INFO, ">>> Received message: {0}", message);

        var msg = new BaseMessageDecoder(message).decode();
        final String strCommand = msg.getCommand();
        Context context = createCommandContext(msg, session);
        CommandController commandController = new CommandControllerImpl(strCommand);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            commandController.handleRequest(context);
        });
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
    }

    private CommandContext createCommandContext(@NonNull BaseMessage message, Session session) {
        var context = new DefaultCommandContext();

        if (message instanceof OkMessage okMessage) {
            String eventId = okMessage.getEventId();
            boolean result = okMessage.getFlag();
            String msg = okMessage.getMessage();

            context.setMessage(msg);
            context.setEventId(eventId);
            context.setResult(result);

        } else if (message instanceof NoticeMessage noticeMessage) {
            String msg = noticeMessage.getMessage();

            context.setMessage(msg);

        } else if (message instanceof EventMessage eventMessage) {
            var subId = eventMessage.getSubscriptionId();
            var jsonEvent = new BaseEventEncoder((BaseEvent) eventMessage.getEvent()).encode();

            context.setSubscriptionId(subId);
            context.setEventId(jsonEvent);

        } else if (message instanceof RelayAuthenticationMessage authMessage) {

            log.log(Level.INFO, "--- onAuth event. Message: {0}", authMessage);

            var challenge = authMessage.getChallenge();

            context.setChallenge(challenge);

        } else if (message instanceof EoseMessage eoseMessage) {
            var subId = eoseMessage.getSubscriptionId();

            context.setSubscriptionId(subId);

        } else {
            throw new RuntimeException("Invalid relay message.");
        }

        // Pass on the private key
        context.setPrivateKey(((DefaultRequestContext) this.context).getPrivateKey());

        // Pass on the first relay
        //var relays = ((DefaultRequestContext) this.context).getRelays();
        context.setRelay(getRelay(session));

        return context;
    }

    private void savePNGImage(byte[] payload, int offset, int length) {
        log.log(Level.FINEST, "savePNGImage");
    }
}
