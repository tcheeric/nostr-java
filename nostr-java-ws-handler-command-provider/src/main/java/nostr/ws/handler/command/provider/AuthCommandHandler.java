package nostr.ws.handler.command.provider;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.PrivateKey;
import nostr.base.annotation.DefaultHandler;
import nostr.client.Client;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultCommandContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.id.Identity;
import nostr.ws.handler.command.CommandHandler;

import java.util.Map;
import java.util.logging.Level;

@DefaultHandler(command = Command.AUTH)
@NoArgsConstructor
@Log
public class AuthCommandHandler implements CommandHandler {

    @Override
    public void handle(@NonNull CommandContext context) {
        log.log(Level.INFO, "+++ onAuth event - {0}", context);

        if (context instanceof DefaultCommandContext defaultCommandContext) {
            var privateKey = defaultCommandContext.getPrivateKey();
            var identity = Identity.getInstance(new PrivateKey(privateKey));
            var publicKey = identity.getPublicKey();
            var challenge = defaultCommandContext.getChallenge();
            var relay = defaultCommandContext.getRelay();
            var relayHostname = relay.getHostname();
            var relayPort = relay.getPort();

            // Create the event
            var canonicalAuthenticationEvent = new CanonicalAuthenticationEvent(publicKey, challenge, relay);

            // Sign the event
            identity.sign(canonicalAuthenticationEvent);

            // Create the request context
            var requestContext = new DefaultRequestContext();
            requestContext.setPrivateKey(privateKey);
            requestContext.setChallenge(challenge);
            requestContext.setRelays(Map.of("relay", relayHostname + ":" + relayPort));

            var client = Client.getInstance(requestContext);
            var canonicalAuthenticationMessage = new CanonicalAuthenticationMessage(canonicalAuthenticationEvent);
            var encoder = new BaseMessageEncoder(canonicalAuthenticationMessage);
            var encodedMessage = encoder.encode();
            log.log(Level.INFO, "Sending authentication event {0} to the relay {1}", new Object[]{encodedMessage, relay});

            // Publish the event to the relay
            client.send(canonicalAuthenticationMessage, relay);
        } else {
            throw new IllegalArgumentException("Invalid context type");
        }
    }
}
