package nostr.connection.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.Connection;
import nostr.context.Context;
import nostr.context.impl.DefaultRequestContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

@Getter
@Log
public class ConnectionPool {

    private static class Holder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();
    }

    private final Set<Connection> connections = Collections.synchronizedSet(new HashSet<>());

    private ConnectionPool() {
        // private constructor to prevent instantiation
    }

    public static ConnectionPool getInstance(@NonNull Context context) {
        if (Holder.INSTANCE.connections.isEmpty() && context instanceof DefaultRequestContext defaultRequestContext) {
            var relays = defaultRequestContext.getRelays();
            relays.values().stream().map(Relay::new).forEach(r -> Holder.INSTANCE.addConnection(new ConnectionImpl(r, context)));
        }
        return Holder.INSTANCE;
    }

    public void connect() {
        log.log(Level.INFO, "Connecting to relays");
        connections.forEach(c -> {
            c.connect();
        });
    }

    public void disconnect() {
        log.log(Level.INFO, "Disconnecting from relays");
        connections.forEach(connection -> {
            connection.disconnect();
        });
    }

    public void disconnect(@NonNull Relay relay) {
        log.log(Level.INFO, "Disconnecting from {0}...", relay);
        var connection = getConnection(relay);
        if (connection != null) {
            connection.disconnect();
        } else {
            log.log(Level.WARNING, "No connection found for {0}. Ignoring...", relay);
        }
    }

    public Connection getConnection(@NonNull Relay relay) {
        return connections.stream().filter(c -> c.getRelay().getUri().equalsIgnoreCase(relay.getUri())).findFirst().orElse(null);
    }

    public boolean isConnectedTo(@NonNull Relay relay) {
        return connections.stream().filter(c -> c.getRelay().equals(relay)).anyMatch(Connection::isConnected);
    }

    public int connectionCount() {
        return (int) connections.stream().filter(Connection::isConnected).count();
    }

    public void send(@NonNull String message) {
        log.log(Level.FINER, "Connectied to {0} relay(s)...", connections.size());
        connections.forEach(conn -> {
            conn.send(message);
        });
    }

    public void send(@NonNull String message, @NonNull Relay relay) {
        log.log(Level.FINER, "ConnectionPool.send({0}, {1}) / {2} connection(s)", new Object[]{message, relay, connections.size()});
        var connection = getConnection(relay);
        if (connection != null) {
            log.log(Level.FINE, "Trying to send {0} to {1}...", new Object[]{message, relay});
            connection.send(message);
        }
        else {
            log.log(Level.WARNING, "No connection found for {0}", relay);
        }
    }

    private boolean addConnection(@NonNull Connection connection) {
        return connections.add(connection);
    }

    private boolean removeConnection(@NonNull Connection connection) {
        return connections.remove(connection);
    }

    private void removeAllConnections() {
        connections.clear();
    }

}