package nostr.connection.impl.listeners;

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
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.ClosedMessage;
import nostr.event.message.RelayAuthenticationMessage;

import okhttp3.WebSocketListener;
import okhttp3.WebSocket;
import okio.ByteString;

import java.util.logging.Level;

@AllArgsConstructor
@Log
public class TextListener extends WebSocketListener {

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final Relay relay;

    private Context context;

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        // nocheckin
        log.log(Level.INFO, "WebSocket received: " + message + " - Relay: {0}", new Object[]{relay});
        handleReceivedText(message);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        String message = bytes.toString();
        log.log(Level.INFO, "WebSocket received: {0} - Relay: {1}", new Object[]{message, relay});
        handleReceivedText(message);
    }

    private void handleReceivedText(@NonNull String message) {
        log.log(Level.INFO, "Received message {0} from {1}", new Object[]{message, relay});

        var msg = new BaseMessageDecoder<>().decode(message);
        final String strCommand = msg.getCommand();

        log.log(Level.FINE, "Creating the command context with message {0}", new Object[]{msg});
        Context commandContext = createCommandContext(msg);
        var applicationController = new ApplicationControllerImpl(strCommand);

        applicationController.handleRequest(commandContext);
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
            log.log(Level.FINE, "Setting the challenge {0} for the relay {1}", new Object[]{authMessage.getChallenge(), relay});
            commandContext.setChallenge(authMessage.getChallenge());
        }

        if (message instanceof ClosedMessage) {
            commandContext.setChallenge(((DefaultRequestContext) this.context).getChallenge(relay));
        }

        return commandContext;
    }
}
