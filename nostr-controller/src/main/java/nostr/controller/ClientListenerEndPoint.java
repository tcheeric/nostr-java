package nostr.controller;

import nostr.controller.handler.CloseHandler;
import nostr.controller.handler.ConnectHandler;
import nostr.controller.handler.ErrorHandler;
import nostr.controller.handler.response.BaseResponseHandler;
import nostr.controller.handler.response.EoseResponseHandler;
import nostr.controller.handler.response.EventResponseHandler;
import nostr.controller.handler.response.NoticeResponseHandler;
import nostr.controller.handler.response.OkResponseHandler;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import java.io.IOException;
import java.util.logging.Level;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.controller.handler.response.OkResponseHandler.Reason;
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
@WebSocket
@NoArgsConstructor
@Log
public class ClientListenerEndPoint {

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        log.fine("onConnect");

        session.setMaxTextMessageSize(16 * 1024);

        ConnectHandler.builder().build().process();
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) throws IOException {
        log.log(Level.FINE, "onClose {0}, {1}", new Object[]{statusCode, reason});

        CloseHandler.builder().reason(reason).statusCode(statusCode).build().process();

        disposeResources();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        log.fine("onError");

        log.log(Level.SEVERE, "An error has occured", cause);

        ErrorHandler.builder().cause(cause).build().process();

        // You may dispose resources.
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
        final String command = ((ArrayValue) jsonArr).get(0).toString();
        String msg; //= ((ArrayValue) jsonArr).get(1).toString();
        BaseResponseHandler responseHandler = null;
        switch (command) {
            case "\"EOSE\"" -> {
                msg = ((ArrayValue) jsonArr).get(1).toString();
                responseHandler = new EoseResponseHandler(msg);
            }
            case "\"OK\"" -> {
                String eventId = ((ArrayValue) jsonArr).get(1).toString();
                boolean result = Boolean.parseBoolean(((ArrayValue) jsonArr).get(2).toString());
                msg = ((ArrayValue) jsonArr).get(3).toString();
                final int colonIndex = msg.indexOf(":") - 1;
                Reason reason = Reason.valueOf(msg.substring(0, colonIndex));
                responseHandler = new OkResponseHandler(eventId, result, reason, msg.substring(colonIndex + 1));
            }
            case "\"NOTICE\"" -> {
                msg = ((ArrayValue) jsonArr).get(1).toString();
                responseHandler = new NoticeResponseHandler(msg);
            }
            case "\"EVENT\"" -> {
                String subId = ((ArrayValue) jsonArr).get(1).toString();
                String jsonEvent = ((ArrayValue) jsonArr).get(2).toString();
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
    public void onBinaryMessage(byte[] payload, int offset, int length) throws IOException {
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
