package nostr.connection.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.connection.Connection;
import nostr.context.Context;
import nostr.context.impl.DefaultRequestContext;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
@Log
public class ConnectionPool {

    private static final ThreadLocal<ConnectionPool> instance = new ThreadLocal<>();

    private final Set<Connection> connections = new HashSet<>();

    private ConnectionPool(Context context) {
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

        // NOTE: Make sure you are waiting enough time for the websocket to connect and start sending data
        for(;;) {}
    }

    public void connect(@NonNull Relay relay) {
        log.log(Level.INFO, "Connecting to {0}...", relay);
        Connection connection = getConnection(relay);
        if (connection != null) {
            connection.connect();

            // NOTE: Make sure you are waiting enough time for the websocket to connect and start sending data
            for(;;) {}
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
        return connections.stream().filter(connection -> connection.getRelay().equals(relay)).filter(Connection::isConnected).findFirst().isPresent();
    }

    public int connectionCount() {
        return connections.stream().filter(Connection::isConnected).collect(Collectors.toList()).size();
    }

    public void send(@NonNull String message) {
        log.log(Level.INFO, ">>> Sending {0} to {1} relay(s)...", new Object[]{message, connections.size()});
        connections.forEach(conn -> conn.send(message));
    }

    public void send(@NonNull String message, @NonNull Relay relay) {
        log.log(Level.INFO, ">>> Sending {0} to {1}...", new Object[]{message, relay});
        Connection connection = getConnection(relay);
        if (connection != null) {
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
