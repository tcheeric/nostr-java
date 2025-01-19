package nostr.command.provider;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.PrivateKey;
import nostr.base.annotation.DefaultHandler;
import nostr.client.springwebsocket.WebSocketClientIF;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultCommandContext;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.id.Identity;

import java.io.IOException;
import java.util.logging.Level;

@DefaultHandler(command = Command.AUTH)
@Log
public class AuthCommandHandler extends AbstractCommandHandler {

    public AuthCommandHandler(WebSocketClientIF client) {
        super(client);
    }

    @Override
    public void handle(@NonNull CommandContext context) throws IOException {

        log.log(Level.INFO, "onAuth event - {0}", context);

        if (context instanceof DefaultCommandContext defaultCommandContext) {
            var message = defaultCommandContext.getMessage();

            if (message instanceof RelayAuthenticationMessage) {
                log.log(Level.INFO, "Authentication required on relay {0}", defaultCommandContext.getRelay());

                var privateKey = defaultCommandContext.getPrivateKey();
                var identity = Identity.create(new PrivateKey(privateKey));
                var publicKey = identity.getPublicKey();
                var challenge = defaultCommandContext.getChallenge();
                var relay = defaultCommandContext.getRelay();

                // Create the event
                var canonicalAuthenticationEvent = new CanonicalAuthenticationEvent(publicKey, challenge, relay);

                // Sign the event
                identity.sign(canonicalAuthenticationEvent);

                var canonicalAuthenticationMessage = new CanonicalAuthenticationMessage(canonicalAuthenticationEvent);
                var encodedMessage = canonicalAuthenticationMessage.encode();
                log.log(Level.INFO, "Sending authentication event {0} to the relay {1}", new Object[]{encodedMessage, relay});

                // Publish the event to the relay
                getClient().send(canonicalAuthenticationMessage);
            }
        } else {
            throw new IllegalArgumentException("Invalid context type");
        }
    }
}
