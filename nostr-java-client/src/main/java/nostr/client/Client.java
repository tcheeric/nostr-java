package nostr.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.impl.ConnectionPool;
import nostr.context.Context;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericTag;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.util.thread.Task;
import nostr.util.thread.ThreadUtil;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Log
@NoArgsConstructor
public class Client {

    private static class Holder {
        private static final Client INSTANCE = new Client();
    }

    private RequestContext context;

    private ConnectionPool connectionPool;

    public static Client getInstance() {
        return Holder.INSTANCE;
    }

    public Client connect(@NonNull RequestContext context) throws TimeoutException {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            Holder.INSTANCE.context = context;
            connectionPool = ConnectionPool.getInstance(defaultRequestContext);
            ThreadUtil.builder().blocking(true).lock(true).task(new RelayConnectionTask(this.connectionPool)).build().run(context);
        }
        return this;
    }

    public void disconnect() throws TimeoutException {
        ThreadUtil.builder().blocking(true).task(new RelayDisconnectionTask(this.connectionPool)).build().run(this.context);
    }

    public int getOpenConnectionsCount() {
        return connectionPool.connectionCount();
    }

    public void send(@NonNull BaseMessage message) throws TimeoutException {
        log.log(Level.FINE, "Requesting to send the message {0}...", message);
        ThreadUtil.builder().blocking(false).lock(true).task(new SendMessageTask(message, this.connectionPool)).build().run(this.context);
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

        @SneakyThrows
        @Override
        public Void execute(@NonNull Context context) {
            //  Only send AUTH messages to the relay mentioned in the tag https://github.com/tcheeric/nostr-java/issues/129
            if (message instanceof CanonicalAuthenticationMessage canonicalAuthenticationMessage) {
                log.log(Level.FINE, ">>> Authentication message {0}...", canonicalAuthenticationMessage);
                var event = canonicalAuthenticationMessage.getEvent();
                var relayTag = event.getTags().stream().filter(t -> t.getCode().equalsIgnoreCase("relay")).findFirst();
                if (relayTag.isPresent()) {
                    var relayTagValue = ((GenericTag) relayTag.get()).getAttributes().get(0).getValue().toString();
                    log.log(Level.FINEST, "**** Relay found in CanonicalAuthenticationMessage: {0}", relayTagValue);
                    var r = new Relay(relayTagValue);
                    connectionPool.send(canonicalAuthenticationMessage.encode(), r);
                } else {
                    log.log(Level.SEVERE, "Relay tag not found in CanonicalAuthenticationMessage. Ignoring...");
                }
            } else {
                log.log(Level.FINER, "+++ message {0}...", message);
                connectionPool.send(message.encode());
            }
            return null;
        }
    }
}