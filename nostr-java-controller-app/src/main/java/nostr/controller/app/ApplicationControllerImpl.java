package nostr.controller.app;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.context.Context;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.controller.ApplicationController;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import nostr.ws.Connection;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

@Log
public class ApplicationControllerImpl implements ApplicationController {

    private Connection connection;
    private ExecutorService executorService;

    private final BaseMessage message;

    public ApplicationControllerImpl(@NonNull BaseMessage message) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.message = message;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void handleRequest(@NonNull Context requestContext) {
        requestContext.validate();
        try {
            if (requestContext instanceof DefaultRequestContext defaultRequestContext) {
                executorService.submit(() -> {
                    try {
                        sendMessage(defaultRequestContext);
                    } catch (NostrException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void sendMessage(@NonNull RequestContext context) throws IOException, NostrException {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            var relayMap = defaultRequestContext.getRelays();
            var relayList = toRelayList(relayMap);
            relayList.stream().forEach(relay -> {
                try {
                    this.connection = new Connection(relay, defaultRequestContext, new ArrayList<>());
                    //var message = defaultRequestContext.getMessage();

                    final Session session = this.connection.getSession();
                    if (session != null) {
                        RemoteEndpoint remote = session.getRemote();

                        final String msg = new BaseMessageEncoder(message, relay).encode();

                        log.log(Level.INFO, "Sending Message: {0}", msg);
                        remote.sendString(msg);

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            throw new NostrException("Could not get a session");
        }
        throw new NostrException("Invalid context type");
    }

    private static List<Relay> toRelayList(Map<String, String> relaysMap) {
        List<Relay> relays = new ArrayList<>();
        relaysMap.forEach((name, hostname) -> relays.add(Relay.fromString(NostrUtil.serverURI(hostname).toString())));
        return relays;
    }

}
