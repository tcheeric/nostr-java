package nostr.client.springwebsocket;

/**
 * Connection states for a WebSocket relay connection.
 */
public enum ConnectionState {
  CONNECTING,
  CONNECTED,
  RECONNECTING,
  CLOSED
}
