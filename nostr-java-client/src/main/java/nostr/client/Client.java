package nostr.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.stream.Collectors;

import lombok.Getter;
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

@Log
public class Client {

    private static Client INSTANCE;

	private final List<Future<Relay>> futureRelays = new ArrayList<>();
	private final IRequestHandler requestHandler = new DefaultRequestHandler();
//	TODO: remove getter
	@Getter
	private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

	public Client() throws IOException {
        this.init();
    }

	public Client(Map<String, String> relays) {
        this.init(relays);
    }

    public static Client getInstance() {
        if (INSTANCE == null) {
			try {
				INSTANCE = new Client();
			} catch (IOException ex) {
				log.log(Level.SEVERE, null, ex);
				throw new RuntimeException(ex);
			}
        }

		return INSTANCE.waitConnection();
    }

    public static Client getInstance(Map<String, String> relays) {
		INSTANCE = (INSTANCE == null) ? new Client(relays) : INSTANCE;

		return INSTANCE.waitConnection();
    }

	public Client waitConnection() {
		do {
			try {
				log.log(Level.INFO, "Waiting for relays' connections to open...");
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		} while (this.threadPool.getCompletedTaskCount() < (this.getRelays().size() / 2));

		return this;
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

        log.log(Level.INFO, "Sending message {0}", message);

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

        log.log(Level.FINER, "Authenticating {0}", identity);
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

    private Relay openRelay(@NonNull String name, @NonNull String hostname) {
        var serverURI = serverURI(hostname);
        var relay = Relay.fromString(serverURI.toString());

        var rid = Relay.RelayInformationDocument.builder().name(name).build();
        relay.setInformationDocument(rid);
        updateRelayInformation(relay);

        return relay;
    }

    public static URI serverURI(String hostname) {
        try {
            URL url = new URI("https://" + hostname).toURL();

            URLConnection openConnection = url.openConnection();

            log.log(Level.FINE, "Openning a secure connection to {0}", hostname);

            openConnection.connect();
            return new URI("wss://" + hostname);
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, null, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.log(Level.WARNING, String.format("It wasn't possible to connect to server %s using HTTPS, trying with HTTP...", hostname));
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, String.format("Invalid URI: %s", hostname), e);
            throw new RuntimeException(e);
        }

        try {
            URL url = new URI("http://" + hostname).toURL();

            URLConnection openConnection = url.openConnection();

            log.log(Level.FINE, "Opening an un-secure connection to {0}", hostname);

            openConnection.connect();

            return new URI("ws://" + hostname);
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, null, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.log(Level.WARNING, String.format("It wasn't possible to connect to server %s using HTTP", hostname));
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, String.format("Invalid URI: %s", hostname), e);
            throw new RuntimeException(e);
        }

//    	TODO
        throw new RuntimeException();
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
        relayList.forEach(r -> relays.put(r.getName(), r.getHostname()));
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
            super("", CONFIG_TYPE_RELAY);
        }

        List<Relay> getRelays() {
            Set<Object> relays = this.properties.keySet();
            List<Relay> result = new ArrayList<>();

            relays.forEach(r -> {
                var rid = Relay.RelayInformationDocument.builder().name(r.toString()).build();
                var hostname = this.getProperty(r.toString());
                var relay = new Relay(hostname, rid);
                result.add(relay);
            });
            return result;
        }
    }
}
