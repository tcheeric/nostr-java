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
import java.util.concurrent.TimeoutException;
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
            try {
                c.connect();
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void connect(@NonNull Relay relay) throws TimeoutException {
        log.log(Level.INFO, "Connecting to {0}...", relay);
        var connection = getConnection(relay);
        if (connection == null) {
            connection.connect();
        } else {
            log.log(Level.WARNING, "Already connected to {0}. Ignoring...", relay);
        }
    }

    public void disconnect() {
        log.log(Level.INFO, "Disconnecting from relays");
        connections.forEach(connection -> {
            try {
                connection.disconnect();
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void disconnect(@NonNull Relay relay) {
        log.log(Level.INFO, "Disconnecting from {0}...", relay);
        var connection = getConnection(relay);
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.log(Level.WARNING, "No connection found for {0}. Ignoring...", relay);
        }
    }

    public Connection getConnection(@NonNull Relay relay) {
        return connections.stream().filter(c -> c.getRelay().getUri().toLowerCase().equals(relay.getUri().toLowerCase())).findFirst().orElse(null);
    }

    public boolean isConnectedTo(@NonNull Relay relay) {
        return connections.stream().filter(c -> c.getRelay().equals(relay)).anyMatch(Connection::isConnected);
    }

    public int connectionCount() {
        return (int) connections.stream().filter(Connection::isConnected).count();
    }

    public void send(@NonNull String message) {
        log.log(Level.INFO, ">>> Connectied to {0} relay(s)...", connections.size());
        connections.forEach(conn -> {
            try {
                conn.send(message);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void send(@NonNull String message, @NonNull Relay relay) {
        log.log(Level.INFO, ">>> ConnectionPool.send({0}, {1}) / {2} connection(s)", new Object[]{message, relay, connections.size()});
        var connection = getConnection(relay);
        if (connection != null) {
            log.log(Level.INFO, ">>> Trying to send {0} to {1}...", new Object[]{message, relay});
            try {
                connection.send(message);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            log.log(Level.WARNING, ">>> No connection found for {0}", relay);
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