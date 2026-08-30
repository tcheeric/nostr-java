package nostr.mcp.transport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the transport defaults to being unreachable from the network.
 *
 * <p>It carries no authentication, so where it listens is the only thing standing between an
 * agent's keys and anyone who can route to the host.
 */
class BindAddressTest {

  // Verifies the default is loopback, so a server started with no configuration is not exposed.
  @Test
  void theDefaultIsLoopback() {
    assertEquals("127.0.0.1", BindAddress.fromConfiguredValue(null).host());
    assertTrue(BindAddress.fromConfiguredValue(null).isLoopback());
    assertTrue(BindAddress.fromConfiguredValue("  ").isLoopback());
  }

  // Verifies the other loopback spellings are recognised, since a deployer writing 'localhost'
  // has made the safe choice and should not be warned as though they had not.
  @Test
  void theOtherLoopbackSpellingsAreRecognised() {
    assertTrue(BindAddress.fromConfiguredValue("localhost").isLoopback());
    assertTrue(BindAddress.fromConfiguredValue("127.0.0.1").isLoopback());
    assertTrue(BindAddress.fromConfiguredValue("::1").isLoopback());
  }

  // Verifies a wildcard or public bind is not treated as loopback, since that is exactly the
  // case the warning exists for.
  @Test
  void aWildcardBindIsNotLoopback() {
    assertFalse(BindAddress.fromConfiguredValue("0.0.0.0").isLoopback());
    assertFalse(BindAddress.fromConfiguredValue("192.168.1.10").isLoopback());
  }

  // Verifies an address that cannot be resolved is treated as unsafe, since assuming the
  // generous interpretation of something unparseable is how a server ends up exposed.
  @Test
  void anUnresolvableAddressIsTreatedAsUnsafe() {
    assertFalse(BindAddress.fromConfiguredValue("not a host name at all").isLoopback());
  }
}
