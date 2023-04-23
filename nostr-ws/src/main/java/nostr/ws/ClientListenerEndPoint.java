package nostr.ws;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.logging.Level;

import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.base.BaseConfiguration;
import nostr.base.Relay;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.types.values.impl.ArrayValue;
import nostr.util.NostrException;
import nostr.ws.base.handler.response.IAuthResponseHandler;
import nostr.ws.base.handler.response.IEoseResponseHandler;
import nostr.ws.base.handler.response.IEventResponseHandler;
import nostr.ws.base.handler.response.INoticeResponseHandler;
import nostr.ws.base.handler.response.IOkResponseHandler;
import nostr.ws.base.handler.response.IOkResponseHandler.Reason;
import nostr.ws.base.handler.response.IResponseHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import nostr.ws.handler.response.DefaultAuthResponseHandler;
import nostr.ws.handler.response.DefaultEoseResponseHandler;
import nostr.ws.handler.response.DefaultEventResponseHandler;
import nostr.ws.handler.response.DefaultNoticeResponseHandler;
import nostr.ws.handler.response.DefaultOkResponseHandler;

/**
 *
 * @author squirrel
 */
@WebSocket(idleTimeout = Integer.MAX_VALUE)
@NoArgsConstructor
@Log
public class ClientListenerEndPoint {

    private IResponseHandler responseHandler;

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
    public void onTextMessage(Session session, String message) throws IOException, NostrException {

        if ("close".equalsIgnoreCase(message)) {
            session.close(StatusCode.NORMAL, "bye");
            return;
        }

        log.log(Level.FINE, "onTextMessage Relay {0}: Message: {1}", new Object[]{session.getRemoteAddress(), message});

        ArrayValue jsonArr = new JsonArrayUnmarshaller(message).unmarshall();
        final String command = (jsonArr).get(0).get().getValue().toString();
        String msg;

        switch (command) {
            case "EOSE" -> {
                msg = (jsonArr).get(1).get().getValue().toString();

                responseHandler = createEoseResponseHandler();
                ((IEoseResponseHandler) responseHandler).setSubscriptionId(msg);
            }
            case "OK" -> {
                String eventId = (jsonArr).get(1).get().getValue().toString();
                boolean result = Boolean.parseBoolean((jsonArr).get(2).toString());
                msg = (jsonArr).get(3).get().getValue().toString();
                final var msgSplit = msg.split(":", 2);
                Reason reason;
                String reasonMessage = msg;
                if (msgSplit.length < 2) {
                    reason = Reason.UNDEFINED;
                } else {
                    reason = Reason.fromCode(msgSplit[0]).orElseThrow(RuntimeException::new);
                    reasonMessage = msgSplit[1];
                }

                responseHandler = createOkResponseHandler();
                ((IOkResponseHandler) responseHandler).setEventId(eventId);
                ((IOkResponseHandler) responseHandler).setMessage(msg);
                ((IOkResponseHandler) responseHandler).setReason(reason);
                ((IOkResponseHandler) responseHandler).setResult(result);
            }
            case "NOTICE" -> {
                msg = jsonArr.get(1).get().getValue().toString();
                responseHandler = createNoticeResponseHandler();
                ((INoticeResponseHandler) responseHandler).setMessage(msg);
            }
            case "EVENT" -> {
                String subId = jsonArr.get(1).get().getValue().toString();
                String jsonEvent = jsonArr.get(2).get().toString();

                log.log(Level.FINE, "jsonEvent: {0}", jsonEvent);

                responseHandler = createEventResponseHandler();
                ((IEventResponseHandler) responseHandler).setJsonEvent(jsonEvent);
                ((IEventResponseHandler) responseHandler).setSubscriptionId(subId);
            }
            case "AUTH" -> {
                String challenge = jsonArr.get(1).get().getValue().toString();
                responseHandler = createAuthResponseHandler();
                ((IAuthResponseHandler) responseHandler).setChallenge(challenge);
                ((IAuthResponseHandler) responseHandler).setRelay(getRelay(session));
            }
            default -> {
            }
        }

        if (responseHandler != null) {
            responseHandler.process();
        }
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

    private void disposeResources() {
        log.log(Level.FINE, "disposeResources");
    }

    private void savePNGImage(byte[] payload, int offset, int length) {
        log.log(Level.FINE, "savePNGImage");
    }

    private IEoseResponseHandler createEoseResponseHandler() {
        try {
            var config = new HandlerConfiguration();
            var strClass = config.getEoseResponseHandler();
            return (IEoseResponseHandler) Class.forName(strClass).newInstance();
        } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            log.log(Level.WARNING, null, ex);
            return new DefaultEoseResponseHandler();
        }
    }

    private IOkResponseHandler createOkResponseHandler() {
        try {
            var config = new HandlerConfiguration();
            var strClass = config.getOkResponseHandler();
            return strClass == null ? new DefaultOkResponseHandler() : (IOkResponseHandler) Class.forName(strClass).newInstance();
        } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            log.log(Level.WARNING, null, ex);
            return new DefaultOkResponseHandler();
        }
    }

    private INoticeResponseHandler createNoticeResponseHandler() {
        try {
            var config = new HandlerConfiguration();
            var strClass = config.getNoticeResponseHandler();
            return strClass == null ? new DefaultNoticeResponseHandler() : (INoticeResponseHandler) Class.forName(strClass).newInstance();
        } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            log.log(Level.WARNING, null, ex);
            return new DefaultNoticeResponseHandler();
        }
    }

    private IEventResponseHandler createEventResponseHandler() {
        try {
            var config = new HandlerConfiguration();
            var strClass = config.getEventResponseHandler();
            return strClass == null ? new DefaultEventResponseHandler() : (IEventResponseHandler) Class.forName(strClass).newInstance();
        } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            log.log(Level.WARNING, null, ex);
            return new DefaultEventResponseHandler();
        }
    }

    private IAuthResponseHandler createAuthResponseHandler() {
        try {
            var config = new HandlerConfiguration();
            var strClass = config.getAuthResponseHandler();
            return strClass == null ? new DefaultAuthResponseHandler() : (IAuthResponseHandler) Class.forName(strClass).newInstance();
        } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            log.log(Level.WARNING, null, ex);
            return new DefaultAuthResponseHandler();
        }
    }

    private Relay getRelay(Session session) {
        SocketAddress remoteAddress = session.getRemoteAddress();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
        String remoteHostname = inetSocketAddress.getHostName();
        return Relay.builder().uri(remoteHostname).build();
    }

    static class HandlerConfiguration extends BaseConfiguration {

        HandlerConfiguration() throws IOException {
//        	TODO
            this("/handlers.properties");
        }

        HandlerConfiguration(String file) throws IOException {
            super(file);
        }

        String getEoseResponseHandler() {
            return getProperty("eose.handler");
        }

        String getOkResponseHandler() {
            return getProperty("ok.handler");
        }

        String getNoticeResponseHandler() {
            return getProperty("notice.handler");
        }

        String getEventResponseHandler() {
            return getProperty("event.handler");
        }

        String getAuthResponseHandler() {
            return getProperty("auth.handler");
        }
    }
}
