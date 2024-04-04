package nostr.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.controller.ApplicationController;
import nostr.controller.app.ApplicationControllerImpl;
import nostr.event.BaseMessage;
import nostr.util.NostrUtil;
import nostr.ws.ClientListenerEndPoint;
import nostr.ws.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class Client {

    private static Client INSTANCE;

    private final List<Future<Relay>> futureRelays = new ArrayList<>();

    @Getter
    private final List<BaseMessage> responses = new ArrayList<>();

    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    private RequestContext context;

    private final List<Connection> connections = new ArrayList<>();

    private Client() {
        if (this.threadPool.getCompletedTaskCount() == 0) {
            this.openRelays();
        }
    }

    private Client(@NonNull RequestContext context) {
        if (context instanceof DefaultRequestContext) {
            this.context = context;
            if (this.threadPool.getCompletedTaskCount() == 0) {
                this.openRelays();
            }
        }
    }

    public static Client getInstance(@NonNull RequestContext context) {
        INSTANCE = (INSTANCE == null) ? new Client(context) : INSTANCE;
        return INSTANCE.waitConnection();
    }

    public static Client getInstance() {
        INSTANCE = (INSTANCE == null) ? new Client() : INSTANCE;
        return INSTANCE.waitConnection();
    }

    public Client waitConnection() {
        do {
/*

            try {
                //log.log(Level.INFO, "Waiting for relays' connections to open...");
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
*/
        } while (this.threadPool.getCompletedTaskCount() < (this.getRelays().size() / 2));

        return this;
    }

/*
    public Client connect(@NonNull RequestContext context) {
        INSTANCE = new Client(context);
        return waitConnection();
    }
*/

    public void disconnect() {
        this.threadPool.shutdown();
        this.connections.forEach(c -> c.stop(context));
    }

    public int getOpenSessionsCount() {
        return ClientListenerEndPoint.getInstance(context).getActiveSessions().size();
    }

    private List<Relay> getRelays() {
        return futureRelays.parallelStream()
                .filter(fr -> {
                    try {
                        return fr.isDone();
                    } catch (Exception e) {
                        log.log(Level.WARNING, null, e);
                    }

                    return false;
                }).map(fr -> {
                    try {
                        return fr.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.log(Level.SEVERE, null, e);
                    }

                    return null;
                }).collect(Collectors.toList());
    }

    public void send(@NonNull BaseMessage message) {
        if (context instanceof DefaultRequestContext) {

            log.log(Level.INFO, "Sending message {0}...", message);

            futureRelays.parallelStream()
                    .filter(fr -> {
                        try {
                            return fr.isDone() && /*fr.get().getSupportedNips().contains(message.getNip()) &&*/ isConnected(fr.get());
                        } catch (InterruptedException | ExecutionException e) {
                            log.log(Level.WARNING, null, e);
                            return false;
                        }
                    })
                    .forEach(fr -> {
                        try {
                            Relay r = fr.get();
                            send(message, r);
                        } catch (InterruptedException | ExecutionException ex) {
                            log.log(Level.SEVERE, null, ex);
                        }
                    });
        }
    }

    public void send(@NonNull BaseMessage message, @NonNull Relay relay) {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            if (defaultRequestContext.getRelays().containsValue(relay.getHostname())) {
                if (isConnected(relay)) {
                    log.log(Level.INFO, "Sending message to relay {0}", relay);
                    ApplicationController applicationController = new ApplicationControllerImpl(message);
                    applicationController.handleRequest(defaultRequestContext);
                }
            }
        }
    }

    private boolean isConnected(@NonNull Relay relay) {
        ClientListenerEndPoint clientListenerEndPoint = ClientListenerEndPoint.getInstance(context);
        return clientListenerEndPoint.isConnected(relay);
/*
        var connection = connections.stream().filter(r -> r.getRelay().equals(relay)).findFirst();
        if (connection.isPresent()) {
            return connection.get().isOpen();
        } else {
            return false;
        }
*/
    }

    private void openRelays() {
        DefaultRequestContext defaultRequestContext = (DefaultRequestContext) context;
        Map<String, String> relayMap = defaultRequestContext.getRelays();

        relayMap.values().stream().map(r -> Relay.fromString(NostrUtil.serverURI(r).toString())).forEach(r -> {
            openRelay(r);
        });
    }

    private void openRelay(@NonNull Relay relay) {
        Future<Relay> future = this.threadPool.submit(() -> {
            log.log(Level.INFO, "Connecting to {0}...", relay);
            var connection = new Connection(relay, this.context, responses);
            this.connections.add(connection);
            return relay;
        });
        this.futureRelays.add(future);
    }

    private Relay updateRelayInformation(@NonNull Relay relay) {
        try {
            var rid = Relay.RelayInformationDocument.builder().name(relay.getName()).build();
            relay.setInformationDocument(rid);
            var connection = new Connection(relay, this.context, responses);
            //connection.updateRelayMetadata(relay);
            return relay;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
