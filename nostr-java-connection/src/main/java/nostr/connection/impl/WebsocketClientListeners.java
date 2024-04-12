package nostr.connection.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.context.CommandContext;
import nostr.context.Context;
import nostr.context.impl.DefaultCommandContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.controller.impl.ApplicationControllerImpl;
import nostr.event.BaseMessage;
import nostr.event.Response;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.ClosedMessage;
import nostr.event.message.RelayAuthenticationMessage;

import java.net.http.WebSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;

@Getter
@Log
public class WebsocketClientListeners {

    private final Set<Response> responses = Collections.synchronizedSet(new HashSet<>());

    @AllArgsConstructor
    public class OpenListener implements WebSocket.Listener {

        @Getter
        @EqualsAndHashCode.Include
        @ToString.Include
        private final Relay relay;

        @Override
        public void onOpen(WebSocket webSocket) {
            log.log(Level.INFO, "WebSocket opened to {0}", relay);
        }
    }

    @AllArgsConstructor
    public class TextListener implements WebSocket.Listener {

        @Getter
        @EqualsAndHashCode.Include
        @ToString.Include
        private final Relay relay;

        private Context context;

        @Override
        public CompletionStage<Void> onText(WebSocket webSocket, CharSequence data, boolean last) {

            webSocket.request(1L);

            log.log(Level.INFO, "WebSocket received: {0} - Relay: {1}", new Object[]{data, relay});
            return CompletableFuture.completedFuture(data)
                    .thenAccept(o -> handleReceivedText(o));
        }

        private void handleReceivedText(@NonNull CharSequence data) {
            log.log(Level.INFO, "Received message {0} from {1}", new Object[]{data, relay});
            String message = data.toString();

            var msg = new BaseMessageDecoder(message).decode();
            responses.add(Response.builder().message(msg).relay(relay).build());
            final String strCommand = msg.getCommand();

            log.log(Level.INFO, "Creating the command context with message {0}", new Object[]{msg});
            Context commandContext = createCommandContext(msg);
            var applicationController = new ApplicationControllerImpl(strCommand);

            applicationController.handleRequest(commandContext);
            //List<Response> responses = applicationController.getResponses();
            //this.responses.addAll(responses);
        }

        private CommandContext createCommandContext(@NonNull BaseMessage message) {
            var commandContext = new DefaultCommandContext();

            // Pass on the message
            commandContext.setMessage(message);

            // Pass on the private key
            commandContext.setPrivateKey(((DefaultRequestContext) this.context).getPrivateKey());

            // Pass on the first relay
            commandContext.setRelay(relay);

            // Set the challenge
            if (message instanceof RelayAuthenticationMessage authMessage) {
                ((DefaultRequestContext) this.context).setChallenge(relay, authMessage.getChallenge());
            }

            if (message instanceof ClosedMessage closedMessage) {
                commandContext.setChallenge(((DefaultRequestContext) this.context).getChallenge(relay));
            }

            return commandContext;
        }
    }

    @AllArgsConstructor
    public class CloseListener implements WebSocket.Listener {

        @Getter
        @EqualsAndHashCode.Include
        @ToString.Include
        private final Relay relay;

        @Override
        public CompletionStage<Void> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.log(Level.INFO, "WebSocket connection to {0} closed: {1}, {2}", new Object[]{relay, statusCode, reason});
            responses.clear();
            return CompletableFuture.completedFuture(null);
        }
    }

    @AllArgsConstructor
    public class ErrorListener implements WebSocket.Listener {

        @Getter
        @EqualsAndHashCode.Include
        @ToString.Include
        private final Relay relay;

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.log(Level.WARNING, "WebSocket error: {0} - Relay {1}", new Object[]{error.getMessage(), relay});
        }
    }
}
