package nostr.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.impl.ConnectionPool;
import nostr.context.Context;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.event.BaseMessage;
import nostr.event.Response;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.util.thread.Task;
import nostr.util.thread.ThreadUtil;

@Log
@NoArgsConstructor
public class Client {

    private static final ThreadLocal<Client> INSTANCE = new ThreadLocal<>();

    @Getter
    private final List<BaseMessage> responses = new ArrayList<>();

    private RequestContext context;

    private ConnectionPool connectionPool;

    public static Client getInstance() {
        if (INSTANCE.get() == null) {
            INSTANCE.set(new Client());
        }
        return INSTANCE.get();
    }

    public Client connect(@NonNull RequestContext context) throws TimeoutException {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            INSTANCE.get().context = context;
            connectionPool = ConnectionPool.getInstance(defaultRequestContext);
            ThreadUtil.builder().blocking(true).task(new RelayConnectionTask(this.connectionPool)).build().run(context);
        }
        return this;
    }

    public void disconnect() throws TimeoutException {
        ThreadUtil.builder().blocking(true).task(new RelayDisconnectionTask(this.connectionPool)).build().run(this.context);
    }

    public int getOpenConnectionsCount() {
        return connectionPool.connectionCount();
    }

    public CompletableFuture<Set<Response>> getResponsesAsync() {
        return null; //TODO
    }

    public void send(@NonNull BaseMessage message) throws TimeoutException {
        log.log(Level.INFO, "Sending message {0}...", message);
        ThreadUtil.builder().blocking(false).task(new SendMessageTask(message, this.connectionPool)).build().run(this.context);
    }

    public void send(@NonNull BaseMessage message, @NonNull Relay relay) {
        var encoder = new BaseMessageEncoder(message);
        this.connectionPool.send(encoder.encode(), relay);
    }

    boolean isConnected(@NonNull Relay relay) {
        return this.connectionPool.isConnectedTo(relay);
    }

    @AllArgsConstructor
    private static class RelayConnectionTask implements Task<Void> {

        private final ConnectionPool connectionManager;

        @Override
        public Void execute(@NonNull Context context) {
            connectionManager.connect();
            return null;
        }
    }

    @AllArgsConstructor
    private static class RelayDisconnectionTask implements Task<Void> {

        private final ConnectionPool connectionPool;

        @Override
        public Void execute(@NonNull Context context) {
            connectionPool.disconnect();
            return null;
        }
    }

    @AllArgsConstructor
    private static class SendMessageTask implements Task<Void> {

        private final BaseMessage message;
        private final ConnectionPool connectionPool;

        @Override
        public Void execute(@NonNull Context context) {
            //  Only send AUTH messages to the relay mentioned in the tag https://github.com/tcheeric/nostr-java/issues/129
            if (message instanceof CanonicalAuthenticationMessage canonicalAuthenticationMessage) {
                log.log(Level.INFO, ">>> Authentication message {0}...", canonicalAuthenticationMessage);
                var event = canonicalAuthenticationMessage.getEvent();
                var relayTag = event.getTags().stream().filter(t -> t.getCode().equalsIgnoreCase("relay")).findFirst();
                if (relayTag.isPresent()) {
                    var relayTagValue = ((GenericTag) relayTag.get()).getAttributes().get(0).getValue().toString();
                    var r = new Relay(relayTagValue);
                    connectionPool.send(new BaseMessageEncoder(canonicalAuthenticationMessage).encode(), r);
                } else {
                    log.log(Level.SEVERE, "Relay tag not found in CanonicalAuthenticationMessage. Ignoring...");
                }
            } else {
                log.log(Level.INFO, "+++ message {0}...", message);
                connectionPool.send(new BaseMessageEncoder(message).encode());
            }
            return null;
        }
    }
}