package nostr.client.relay;

import java.io.IOException;

/**
 * Creates a {@link RelayConnection} for a relay URI.
 *
 * <p>Coordinating code depends on this factory rather than constructing connections directly,
 * so that tests can supply scripted relay behaviour in place of real WebSocket transport.
 */
@FunctionalInterface
public interface RelayConnectionFactory {

  /**
   * Open a connection to the given relay.
   *
   * @param relayUri the relay WebSocket URI
   * @return a connected {@link RelayConnection}
   * @throws IOException if the relay could not be reached
   */
  RelayConnection connect(String relayUri) throws IOException;
}
