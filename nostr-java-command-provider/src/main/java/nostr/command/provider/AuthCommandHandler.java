package nostr.command.provider;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.PrivateKey;
import nostr.base.annotation.DefaultHandler;
import nostr.client.Client;
import nostr.command.CommandHandler;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultCommandContext;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.id.Identity;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@DefaultHandler(command = Command.AUTH)
@NoArgsConstructor
@Log
public class AuthCommandHandler implements CommandHandler {

    @Override
    public void handle(@NonNull CommandContext context) {

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

                var client = Client.getInstance();
                var canonicalAuthenticationMessage = new CanonicalAuthenticationMessage(canonicalAuthenticationEvent);
                var encoder = new BaseMessageEncoder(canonicalAuthenticationMessage);
                var encodedMessage = encoder.encode();
                log.log(Level.INFO, "Sending authentication event {0} to the relay {1}", new Object[]{encodedMessage, relay});

                // Publish the event to the relay
                try {
                    client.send(canonicalAuthenticationMessage);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid context type");
        }
    }
}
