package nostr.connection.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.Connection;
import nostr.context.Context;
import nostr.context.impl.DefaultRequestContext;

@Getter
@Log
public class ConnectionPool {

    private static final ThreadLocal<ConnectionPool> instance = new ThreadLocal<>();

    private final Set<Connection> connections = new HashSet<>();

    private ConnectionPool(@NonNull Context context) {
        if (context instanceof DefaultRequestContext defaultRequestContext) {
            var relays = defaultRequestContext.getRelays();
            relays.values().stream().map(Relay::new).forEach(r -> addConnection(new ConnectionImpl(r, context)));
        }
    }

    public static ConnectionPool getInstance(@NonNull Context context) {
        if (instance.get() == null) {
            instance.set(new ConnectionPool(context));
        }
        return instance.get();

    }

    public void connect() {
        log.log(Level.INFO, "Connecting to relays");
        connections.forEach(Connection::connect);
    }

    public void connect(@NonNull Relay relay) {
        log.log(Level.INFO, "Connecting to {0}...", relay);
        Connection connection = getConnection(relay);
        if (connection != null) {
            connection.connect();
        }
    }

    public void disconnect() {
        log.log(Level.INFO, "Disconnecting from relays");
        connections.forEach(Connection::disconnect);
    }

    public void disconnect(@NonNull Relay relay) {
        log.log(Level.INFO, "Disconnecting from {0}...", relay);
        Connection connection = getConnection(relay);
        if (connection != null) {
            connection.disconnect();
        }
    }

    public Connection getConnection(@NonNull Relay relay) {
        return connections.stream().filter(connection -> connection.getRelay().equals(relay)).findFirst().orElse(null);
    }

    public boolean isConnectedTo(@NonNull Relay relay) {
        return connections.stream().filter(connection -> connection.getRelay().equals(relay)).anyMatch(Connection::isConnected);
    }

    public int connectionCount() {
        return (int) connections.stream().filter(Connection::isConnected).count();
    }

    public void send(@NonNull String message) {
        log.log(Level.INFO, ">>> Connection {0} relay(s)...", connections.size());
        connections.forEach(conn -> conn.send(message));
    }

    public void send(@NonNull String message, @NonNull Relay relay) {
        Connection connection = getConnection(relay);
        if (connection != null) {
        	log.log(Level.INFO, ">>> Trying to send {0} to {1}...", new Object[]{message, relay});
            connection.send(message);
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
