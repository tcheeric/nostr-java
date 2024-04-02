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
    private final ApplicationController applicationController = new ApplicationControllerImpl();

    private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    private RequestContext context;

    private Connection connection;

    public Client(@NonNull RequestContext context) {
        if (context instanceof DefaultRequestContext) {
            this.context = context;
            this.openRelays();
        }
    }

    public static Client getInstance(@NonNull RequestContext context) {
        INSTANCE = (INSTANCE == null) ? new Client(context) : INSTANCE;

        return INSTANCE.waitConnection();
    }

    public Client waitConnection() {
        do {
/*
            try {
*/
                log.log(Level.INFO, "Waiting for relays' connections to open...");
                //Thread.sleep(5000);
/*
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
*/
        } while (this.threadPool.getCompletedTaskCount() < (this.getRelays().size() / 2));

        return this;
    }

    public Client connect(@NonNull RequestContext context) {
        INSTANCE = new Client(context);
        return waitConnection();
    }

    public void disconnect() {
        this.threadPool.shutdown();
        this.connection.stop();
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

    public void send() {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            BaseMessage message = defaultRequestContext.getMessage();

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
                            log.log(Level.INFO, "Sending message to relay {0}", r);
                            this.applicationController.handleRequest(defaultRequestContext);
                        } catch (InterruptedException | ExecutionException ex) {
                            log.log(Level.SEVERE, null, ex);
                        }
                    });
        }
    }

/*
    public void send(@NonNull IEvent event) {
        EventMessage message = new EventMessage(event);
        ((DefaultRequest) requestHandler).setMessage(message);
        send();
    }

    public void send(@NonNull IEvent event, String subsciptionId) {
        EventMessage message = new EventMessage(event, subsciptionId);
        ((DefaultRequest) requestHandler).setMessage(message);
        send();
    }

    public void send(@NonNull FiltersList filtersList, String subscriptionId) {
        ReqMessage message = new ReqMessage(subscriptionId, filtersList);
        ((DefaultRequest) requestHandler).setMessage(message);
        send();
    }

    public void send(@NonNull String subscriptionId) {
        CloseMessage message = new CloseMessage(subscriptionId);
        ((DefaultRequest) requestHandler).setMessage(message);
        send();
    }

    // TODO - Make private?
    public void send() {

        BaseMessage message = ((DefaultRequest) requestHandler).getMessage();
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
                        log.log(Level.INFO, "Sending message to relay {0}", r);
                        this.requestHandler.execute(r);
                    } catch (InterruptedException | ExecutionException | NostrException ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                });
    }

    public void auth(Identity identity, String challenge) {

        log.log(Level.FINER, "Authenticating {0}", identity);
        List<Relay> relays = getRelayList();
        var event = new ClientAuthenticationEvent(identity.getPublicKey(), challenge, relays);
        BaseMessage authMsg = new ClientAuthenticationMessage(event);
        ((DefaultRequest) requestHandler).setMessage(authMsg);

        identity.sign(event);
        this.send();
    }

    public void auth(String challenge, Relay relay) {
        auth(Identity.getInstance(), challenge, relay);
    }

    public void auth(Identity identity, String challenge, Relay relay) {

        log.log(Level.INFO, "Authenticating...");
        var event = new ClientAuthenticationEvent(identity.getPublicKey(), challenge, relay);
        BaseMessage authMsg = new ClientAuthenticationMessage(event);
        ((DefaultRequest) requestHandler).setMessage(authMsg);

        identity.sign(event);
        this.send();
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
*/

    private void openRelays() {
        DefaultRequestContext defaultRequestContext = (DefaultRequestContext) context;
        Map<String, String> relayMap = defaultRequestContext.getRelays();

        relayMap.values().stream().map(r -> Relay.fromString(NostrUtil.serverURI(r).toString())).forEach(r -> {
            Future<Relay> future = this.threadPool.submit(() -> {
                this.connection = new Connection(this.context, responses);
                return r;
            });
            this.futureRelays.add(future);
        });
    }

    private Relay updateRelayInformation(@NonNull Relay relay) {
        try {
            var rid = Relay.RelayInformationDocument.builder().name(relay.getName()).build();
            relay.setInformationDocument(rid);
            var connection = new Connection(this.context, responses);
            //connection.updateRelayMetadata(relay);
            return relay;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
