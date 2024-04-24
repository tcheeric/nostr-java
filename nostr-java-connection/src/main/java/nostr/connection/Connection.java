package nostr.connection;

import nostr.base.Relay;

import java.util.concurrent.TimeoutException;

public interface Connection {

    void send(String message) throws TimeoutException;

    void connect() throws TimeoutException;

    void disconnect() throws TimeoutException;

    Relay getRelay();

    boolean isConnected();

}
