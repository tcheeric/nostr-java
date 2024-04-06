package nostr.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.context.Context;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.controller.ClientController;
import nostr.controller.app.ClientControllerImpl;
import nostr.event.BaseMessage;
import nostr.event.Response;
import nostr.util.NostrUtil;
import nostr.util.thread.Task;
import nostr.util.thread.ThreadUtil;
import nostr.ws.Connection;
import nostr.ws.RelayClientListenerEndPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class Client {

    private static Client INSTANCE;

    @Getter
    private final List<BaseMessage> responses = new ArrayList<>();

    private RequestContext context;

    private final List<Connection> connections = new ArrayList<>();

    private Client() {
    }

    private Client(@NonNull RequestContext context) {
        if (context instanceof DefaultRequestContext) {
            this.context = context;
            if (this.connections.isEmpty()) {
                ThreadUtil.builder().wait(true).task(new RelayConnectionTask(this.context, this.connections)).build().run(this.context);
            }
        }
    }

    public static Client getInstance(@NonNull RequestContext context) {
        INSTANCE = (INSTANCE == null) ? new Client(context) : INSTANCE;
        //return INSTANCE.waitConnection();
        return INSTANCE;
    }

    public static Client getInstance() {
        INSTANCE = (INSTANCE == null) ? new Client() : INSTANCE;
        //return INSTANCE.waitConnection();
        return INSTANCE;
    }

    public void disconnect() {
        //this.threadPool.shutdown();
        this.connections.forEach(c -> c.stop(context));
    }

    public int getOpenConnectionsCount() {
        this.connections.stream().forEach(c -> log.log(Level.INFO, "Connection: {0}", c));
        return (int) this.connections
                .stream()
                .filter(connection -> isConnected(connection.getRelay(), this.context))
                .count();
    }


    public CompletableFuture<Set<Response>> getResponsesAsync() {
        return CompletableFuture.supplyAsync(() -> RelayClientListenerEndPoint.getInstance(context).getResponses());
    }

    public void send(@NonNull BaseMessage message) {
        if (context instanceof DefaultRequestContext) {

            log.log(Level.INFO, "Sending message {0}...", message);

            var relays = this.connections.stream().filter(c -> isConnected(c.getRelay(), context)).map(Connection::getRelay).collect(Collectors.toList());

            relays.stream().forEach(r -> {
                send(message, r);
                log.log(Level.INFO, "Done!");
            });
        }
    }

    public void send(@NonNull BaseMessage message, @NonNull Relay relay) {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            if (isConnected(relay, defaultRequestContext)) {
                log.log(Level.INFO, "Sending message to relay {0}", relay);
                ClientController clientController = new ClientControllerImpl(message);
                clientController.handleRequest(defaultRequestContext);
                log.log(Level.INFO, "Message sent to relay {0}", relay);
            }
        }
    }

     static boolean isConnected(@NonNull Relay relay, @NonNull RequestContext context) {
        RelayClientListenerEndPoint relayClientListenerEndPoint = RelayClientListenerEndPoint.getInstance(context);
        return relayClientListenerEndPoint.isConnected(relay);
    }

    @AllArgsConstructor
    private static class RelayConnectionTask implements Task<Void> {

        private final Context context;

        @Getter
        private final List<Connection> connections;

        @Override
        public Void execute(@NonNull Context context) {
            if (context instanceof DefaultRequestContext defaultRequestContext) {
                Map<String, String> relayMap = defaultRequestContext.getRelays();

                relayMap.values().stream().map(r -> Relay.fromString(NostrUtil.serverURI(r).toString())).forEach(r -> {
                    openRelay(r);
                });
            }
            return null;
        }

        private void openRelay(@NonNull Relay relay) {
            if (context instanceof DefaultRequestContext defaultRequestContext) {
                if (!isConnected(relay, defaultRequestContext)) {
                    log.log(Level.INFO, "Connecting to {0}...", relay);
                    this.connections.add(new Connection(relay, defaultRequestContext/*, responses*/));
                }
            }
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
