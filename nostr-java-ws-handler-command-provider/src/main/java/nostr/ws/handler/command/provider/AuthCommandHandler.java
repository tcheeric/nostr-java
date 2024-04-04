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
import nostr.event.impl.ClientAuthenticationEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.message.EventMessage;
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
            var clientAuthenticationEvent = new ClientAuthenticationEvent(publicKey, challenge, relay);

            identity.sign(clientAuthenticationEvent);

            var requestContext = new DefaultRequestContext();
            requestContext.setPrivateKey(privateKey);
            requestContext.setChallenge(challenge);
            requestContext.setRelays(Map.of("relay", relayHostname + ":" + relayPort));

            var encoder = new BaseEventEncoder(clientAuthenticationEvent);
            var encodedEvent = encoder.encode();
            log.log(Level.INFO, "Sending authentication event {0} to the relay {1}", new Object[]{encodedEvent, relay});

            Client client = Client.getInstance(requestContext);
            client.send(new EventMessage(clientAuthenticationEvent), relay);
        } else {
            throw new IllegalArgumentException("Invalid context type");
        }
    }
}
