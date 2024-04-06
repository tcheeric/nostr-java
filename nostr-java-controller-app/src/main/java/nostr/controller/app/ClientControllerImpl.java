package nostr.controller.app;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.context.Context;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.controller.ClientController;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.util.NostrUtil;
import nostr.util.thread.ThreadUtil;
import nostr.ws.Connection;
import nostr.ws.RelayClientListenerEndPoint;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Log
public class ClientControllerImpl implements ClientController {

    private Connection connection;
    //private ExecutorService executorService;

    private final BaseMessage message;

    public ClientControllerImpl(@NonNull BaseMessage message) {
        //this.executorService = Executors.newSingleThreadExecutor();
        this.message = message;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void handleRequest(@NonNull Context requestContext) {
        requestContext.validate();
        if (requestContext instanceof DefaultRequestContext defaultRequestContext) {
            ThreadUtil.builder().task(this).build().run(defaultRequestContext);
/*
                executorService.submit(() -> {
                    try {
                        sendMessage(defaultRequestContext);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
*/
        }
    }

    @Override
    public Object execute(@NonNull Context context) {
        if (context instanceof DefaultRequestContext requestContext) {
            try {
                sendMessage(requestContext);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + "[" + message.getCommand() + "]";
    }

    private void sendMessage(@NonNull RequestContext context) throws IOException {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            var relayMap = defaultRequestContext.getRelays();
            var relayList = toRelayList(relayMap);
            relayList.parallelStream().filter(relay -> filterAuthMessage(message, relay)).forEach(relay -> {
                try {
                    this.connection = new Connection(relay, defaultRequestContext/*, new ArrayList<>()*/);

                    var session = RelayClientListenerEndPoint.getInstance(context).getSession(relay);
                    if (session != null) {
                        RemoteEndpoint remote = session.getRemote();

                        final String msg = new BaseMessageEncoder(message, relay).encode();

                        log.log(Level.INFO, "Sending Message: {0}", msg);
                        remote.sendString(msg);
                    } else {
                        throw new RuntimeException("Session not found for relay " + relay + ". Cannot send message.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            throw new RuntimeException("Invalid context type");
        }
    }

    //  Only send AUTH messages to the relay mentioned in the tag https://github.com/tcheeric/nostr-java/issues/129
    private boolean filterAuthMessage(@NonNull BaseMessage message, @NonNull Relay relay) {
        if (message instanceof CanonicalAuthenticationMessage canonicalAuthenticationMessage) {

            log.log(Level.INFO, "Filtering CanonicalAuthenticationMessage...");

            var event = canonicalAuthenticationMessage.getEvent();
            var relayTag = event.getTags().stream().filter(t -> t.getCode().equalsIgnoreCase("relay")).findFirst();
            if (relayTag.isPresent()) {
                log.log(Level.INFO, "Relay tag found... {0}", relayTag.get());
                var relayTagValue = ((GenericTag) relayTag.get()).getAttributes().get(0).getValue().toString();
                var r = Relay.fromString(relayTagValue);
                if (relay.equals(r)) {
                    return true;
                } else {
                    log.log(Level.INFO, "The message {0} is not intended for relay {1}. Skipping...", new Object[]{message, relay});
                    return false;
                }
            }
        }
        return true;
    }

    private static List<Relay> toRelayList(Map<String, String> relaysMap) {
        List<Relay> relays = new ArrayList<>();
        relaysMap.forEach((name, hostname) -> relays.add(Relay.fromString(NostrUtil.serverURI(hostname).toString())));
        return relays;
    }
}
