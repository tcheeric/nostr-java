package nostr.ws;

import nostr.ws.handler.CloseHandler;
import nostr.ws.handler.ConnectHandler;
import nostr.ws.handler.ErrorHandler;
import nostr.ws.handler.response.BaseResponseHandler;
import nostr.ws.handler.response.EoseResponseHandler;
import nostr.ws.handler.response.EventResponseHandler;
import nostr.ws.handler.response.NoticeResponseHandler;
import nostr.ws.handler.response.OkResponseHandler;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import java.io.IOException;
import java.util.logging.Level;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.ws.handler.response.OkResponseHandler.Reason;
import nostr.types.values.impl.ArrayValue;
import nostr.util.NostrException;
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
@NoArgsConstructor
@Log
public class ClientListenerEndPoint {

    @OnWebSocketConnect
    public void onConnect(Session session) {
        log.fine("onConnect");

        session.setMaxTextMessageSize(16 * 1024);

        ConnectHandler.builder().build().process();
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        log.log(Level.FINE, "onClose {0}, {1}", new Object[]{statusCode, reason});

        CloseHandler.builder().reason(reason).statusCode(statusCode).build().process();

        disposeResources();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        log.fine("onError");

        log.log(Level.SEVERE, "An error has occurred: {}", cause.getMessage());

        ErrorHandler.builder().cause(cause).build().process();

        disposeResources();
    }

    @OnWebSocketMessage
    public void onTextMessage(Session session, String message) throws IOException, NostrException {

        if ("close".equalsIgnoreCase(message)) {
            session.close(StatusCode.NORMAL, "bye");
            return;
        }

        log.log(Level.FINE, "onTextMessage: Message: {0}", message);

        ArrayValue jsonArr = new JsonArrayUnmarshaller(message).unmarshall();
        final String command = (jsonArr).get(0).toString();
        String msg;
        BaseResponseHandler responseHandler = null;
        switch (command) {
            case "\"EOSE\"" -> {
                msg = (jsonArr).get(1).toString();
                responseHandler = new EoseResponseHandler(msg);
            }
            case "\"OK\"" -> {
                String eventId = (jsonArr).get(1).toString();
                boolean result = Boolean.parseBoolean((jsonArr).get(2).toString());
                msg = (jsonArr).get(3).toString();
                final int colonIndex = msg.indexOf(":");
                Reason reason;
                String reasonMessage = "";
                if (colonIndex == -1) {
                    reason = Reason.UNDEFINED;
                    reasonMessage = msg;
                } else {
                    reason = Reason.fromCode(msg.substring(1, colonIndex)).orElseThrow(RuntimeException::new);
                    reasonMessage = msg.substring(colonIndex + 1);
                }
                responseHandler = new OkResponseHandler(eventId, result, reason, reasonMessage);
            }
            case "\"NOTICE\"" -> {
                msg = (jsonArr).get(1).toString();
                responseHandler = new NoticeResponseHandler(msg);
            }
            case "\"EVENT\"" -> {
                String subId = (jsonArr).get(1).toString();
                String jsonEvent = (jsonArr).get(2).toString();
                responseHandler = new EventResponseHandler(subId, jsonEvent);
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
}
