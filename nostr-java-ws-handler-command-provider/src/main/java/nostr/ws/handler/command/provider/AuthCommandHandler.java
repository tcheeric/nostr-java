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
            var publicKey = Identity.getInstance(new PrivateKey(privateKey)).getPublicKey();
            var challenge = defaultCommandContext.getChallenge();
            var relay = defaultCommandContext.getRelay();
            var clientAuthenticationEvent = new ClientAuthenticationEvent(publicKey, challenge, relay);

            var requestContext = new DefaultRequestContext();
            requestContext.setPrivateKey(privateKey);
            requestContext.setChallenge(challenge);
            requestContext.setMessage(new EventMessage(clientAuthenticationEvent));

            var relayName = defaultCommandContext.getRelay().getName();
            var relayHostname = defaultCommandContext.getRelay().getHostname();
            var relayPort = defaultCommandContext.getRelay().getPort();

            requestContext.setRelays(Map.of("relay", relayHostname + ":" + relayPort));

            log.log(Level.INFO, "Sending authentication event to the relay. Context: {0}", requestContext);
            Client client = new Client(requestContext);
            client.send();
        } else {
            throw new IllegalArgumentException("Invalid context type");
        }
    }
}
