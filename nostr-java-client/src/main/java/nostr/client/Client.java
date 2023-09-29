package nostr.client;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.IEvent;
import nostr.base.Relay;
import nostr.event.BaseMessage;
import nostr.event.impl.ClientAuthenticationEvent;
import nostr.event.impl.Filters;
import nostr.event.message.ClientAuthenticationMessage;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import nostr.util.AbstractBaseConfiguration;
import nostr.util.NostrException;
import nostr.ws.Connection;
import nostr.ws.handler.spi.IRequestHandler;
import nostr.ws.request.handler.provider.DefaultRequestHandler;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author squirrel
 */
@Log
@Data
public class Client {

    private static Client INSTANCE;

    private final List<Future<Relay>> futureRelays;
    private final ThreadPoolExecutor threadPool;
    private IRequestHandler requestHandler;

    private Client() throws IOException {
        this.futureRelays = new ArrayList<>();
        this.requestHandler = new DefaultRequestHandler();
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.init();
    }

    private Client(Map<String, String> relays) {
        this.futureRelays = new ArrayList<>();
        this.requestHandler = new DefaultRequestHandler();
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.init(relays);
    }

    public static Client getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new Client();

                do {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } while (INSTANCE.getThreadPool().getCompletedTaskCount() < (INSTANCE.getRelays().size() / 2));

            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }

        return INSTANCE;
    }

    public static Client getInstance(Map<String, String> relays) {
        if (INSTANCE == null) {
            INSTANCE = new Client(relays);
        }

        return INSTANCE;
    }

    public List<Relay> getRelays() {
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

    public void send(@NonNull IEvent event) {
        EventMessage message = new EventMessage(event);
        send(message);
    }

    public void send(@NonNull IEvent event, String subsciptionId) {
        EventMessage message = new EventMessage(event, subsciptionId);
        send(message);
    }

    public void send(@NonNull Filters filters, String subscriptionId) {
        ReqMessage message = new ReqMessage(subscriptionId, filters);
        send(message);
    }

    public void send(@NonNull String subscriptionId) {
        CloseMessage message = new CloseMessage(subscriptionId);
        send(message);
    }

    // TODO - Make private?
    public void send(@NonNull BaseMessage message) {
        futureRelays.parallelStream()
                .filter(fr -> {
                    try {
                        return fr.isDone() && fr.get().getSupportedNips().contains(message.getNip());
                    } catch (InterruptedException | ExecutionException e) {
                        log.log(Level.WARNING, null, e);
                        return false;
                    }
                })
                .forEach(fr -> {
                    try {
                        Relay r = fr.get();
                        this.requestHandler.process(message, r);
                    } catch (InterruptedException | ExecutionException | NostrException ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                });
    }

    public void auth(Identity identity, String challenge) throws NostrException {

        log.log(Level.INFO, "Authenticating...");
        List<Relay> relays = getRelayList();
        var event = new ClientAuthenticationEvent(identity.getPublicKey(), challenge, relays);
        BaseMessage authMsg = new ClientAuthenticationMessage(event);

        identity.sign(event);
        this.send(authMsg);
    }

    public void auth(String challenge, Relay relay) throws NostrException {
        auth(Identity.getInstance(), challenge, relay);
    }

    public void auth(Identity identity, String challenge, Relay relay) throws NostrException {

        log.log(Level.INFO, "Authenticating...");
        var event = new ClientAuthenticationEvent(identity.getPublicKey(), challenge, relay);
        BaseMessage authMsg = new ClientAuthenticationMessage(event);

        identity.sign(event);
        this.send(authMsg);
    }

    public Relay getDefaultRelay() {
        List<Relay> relays = getRelays();
        if (!relays.isEmpty()) {
            return relays.get(0);
        }
        throw new RuntimeException("No configured relay list found");
    }

    private List<Relay> getRelayList() {
        List<Relay> result = new ArrayList<>();

        futureRelays.forEach(fr -> {
            try {
                if (!result.contains(fr.get())) {
                    result.add(fr.get());
                }
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        });

        return result;
    }

    private Relay openRelay(@NonNull String name, @NonNull String uri) {
        URI serverURI = Connection.serverURI(uri);
        Relay.RelayInformationDocument rid = Relay.RelayInformationDocument.builder().name(name).build();
        Relay relay = Relay.builder().uri(serverURI.toString()).informationDocument(rid).build();

        return openRelay(relay);
    }

    private Relay openRelay(@NonNull Relay relay) {
        updateRelayInformation(relay);
        log.log(Level.FINE, "Relay connected: {0}", relay);

        return relay;
    }

    private void init(Map<String, String> mapRelays) {
        mapRelays.entrySet().parallelStream().forEach(r -> {
            Future<Relay> future = this.threadPool.submit(() -> this.openRelay(r.getKey(), r.getValue()));

            this.futureRelays.add(future);
        });
    }

    private void init() throws IOException {
        this.init(toMapRelays());
    }

    private Map<String, String> toMapRelays() throws IOException {
        Map<String, String> relays = new HashMap<>();
        List<Relay> relayList = new RelayConfiguration().getRelays();
        relayList.forEach(r -> relays.put(r.getName(), r.getUri()));
        return relays;
    }

    private void updateRelayInformation(@NonNull Relay relay) {
        try {
            var connection = new Connection(relay);
            connection.updateRelayMetadata();
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    @Log
    static class RelayConfiguration extends AbstractBaseConfiguration {

        RelayConfiguration() throws IOException {
            super();
            var configFile = appConfig.getRelaysProperties();
            configFile = configFile.startsWith("/") ? configFile : "/" + configFile;
            load(configFile);
        }

        List<Relay> getRelays() {
            Set<Object> relays = this.properties.keySet();
            List<Relay> result = new ArrayList<>();

            relays.forEach(r -> {
                Relay.RelayInformationDocument rid = Relay.RelayInformationDocument.builder().name(r.toString()).build();
                var relay = Relay.builder().uri(this.getProperty(r.toString())).informationDocument(rid).build();
                result.add(relay);
            });
            return result;
        }
    }
}
