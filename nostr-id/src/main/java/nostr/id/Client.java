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
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.BaseConfiguration;
import nostr.base.Relay;
import nostr.event.impl.GenericMessage;
import nostr.ws.Connection;
import nostr.ws.handler.request.DefaultRequestHandler;

/**
 *
 * @author squirrel
 */
@Log
@Data
public class Client {

    @ToString.Exclude
    private final Set<Future<Relay>> futureRelays;

    @ToString.Exclude
    private final ThreadPoolExecutor threadPool;

    public Client(String relayConfFile) throws IOException {
        this.futureRelays = new HashSet<>();
        
        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.init(relayConfFile);
    }

    public Client(Map<String, String> relays) {
        this.futureRelays = new HashSet<>();

        this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.init(relays);
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
                    }

                    return false;
                })
                .forEach(fr -> {
                    try {
                        Relay r = fr.get();
                        var rh = DefaultRequestHandler.builder().connection(new Connection(r)).message(message).build();
                        log.log(Level.INFO, "Client {0} sending message to {1}", new Object[]{this, r});
                        rh.process();
                    } catch (Exception ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                });
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
