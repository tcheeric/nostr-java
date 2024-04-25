package nostr.connection;

import nostr.base.Relay;

public interface Connection {

    void send(String message);

    void connect();

    void disconnect();

    Relay getRelay();

    boolean isConnected();

}
