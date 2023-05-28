package nostr.id;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.BaseConfiguration;
import nostr.base.Relay;
import nostr.event.impl.ClientAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.message.AuthMessage;
import nostr.util.NostrException;
import nostr.ws.Connection;
import nostr.ws.handler.spi.IRequestHandler;
import nostr.ws.request.handler.provider.DefaultRequestHandler;

/**
 *
 * @author squirrel
 */
@Log
@Data
public class Client {

    private static Client INSTANCE;

    private final Set<Future<Relay>> futureRelays;
    private final ThreadPoolExecutor threadPool;
    private IRequestHandler requestHandler;

    private Client(String relayConfFile) throws IOException {
        this.futureRelays = new HashSet<>();
        this.requestHandler = new DefaultRequestHandler();
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.init(relayConfFile);
    }

    private Client(Map<String, String> relays) {
        this.futureRelays = new HashSet<>();
        this.requestHandler = new DefaultRequestHandler();
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.init(relays);
    }

    public static Client getInstance(String relayConfFile) {
        if (INSTANCE == null) {
            try {
                INSTANCE = new Client(relayConfFile);
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

    public Set<Relay> getRelays() {
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
        }).collect(Collectors.toSet());
    }

    public void send(@NonNull GenericMessage message) {
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
        Set<Relay> relays = getRelaySet();
        GenericEvent event = new ClientAuthenticationEvent(identity.getPublicKey(), challenge, relays);
        GenericMessage authMsg = new AuthMessage(event);

        identity.sign(event);
        this.send(authMsg);
    }

    private Set<Relay> getRelaySet() {
        Set<Relay> result = new HashSet<>();

        futureRelays.stream().forEach(fr -> {
            try {
                result.add(fr.get());
            } catch (InterruptedException | ExecutionException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        });

        return result;
    }

    private Relay openRelay(@NonNull String name, @NonNull String uri) {
        URI serverURI = Connection.serverURI(uri);
        Relay relay = Relay.builder().name(name).uri(serverURI.toString()).build();

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

    private void init(String file) throws IOException {
        this.init(toMapRelays(file));
    }

    private Map<String, String> toMapRelays(String file) throws IOException {
        Map<String, String> relays = new HashMap<>();
        List<Relay> relayList = new RelayConfiguration(file).getRelays();
        relayList.stream().forEach(r -> relays.put(r.getName(), r.getUri()));
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
    static class RelayConfiguration extends BaseConfiguration {

        RelayConfiguration() throws IOException {
            this("/relays.properties");
        }

        RelayConfiguration(String file) throws IOException {
            super(file);
        }

        List<Relay> getRelays() {
            Set<Object> relays = this.properties.keySet();
            List<Relay> result = new ArrayList<>();

            relays.stream().forEach(r -> {
                var relay = Relay.builder().name(r.toString()).uri(this.getProperty(r.toString())).build();
                result.add(relay);
            });
            return result;
        }
    }
}
