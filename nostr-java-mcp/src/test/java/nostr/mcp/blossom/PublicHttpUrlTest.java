package nostr.mcp.blossom;

import nostr.mcp.tool.ToolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The check that keeps an agent from using this server as a way onto its own network.
 *
 * <p>Uses address literals rather than hostnames so the suite proves the predicate rather than
 * the state of a DNS resolver, and so it passes with no network at all.
 */
class PublicHttpUrlTest {

  private final PublicHttpUrl guarded = new PublicHttpUrl(false);

  // Verifies the addresses that matter are refused. 169.254.169.254 is the cloud instance
  // metadata endpoint and the single most valuable target here; the rest are the private
  // ranges an office or cluster network is built from.
  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://127.0.0.1/blob",
        "http://localhost:3000/blob",
        "http://169.254.169.254/latest/meta-data/",
        "http://10.0.0.1/blob",
        "http://172.16.0.1/blob",
        "http://192.168.1.1/blob",
        "http://[::1]/blob",
        "http://[fc00::1]/blob",
        "http://0.0.0.0/blob"
      })
  void refusesAnAddressThatIsNotOnThePublicInternet(String url) {
    ToolException refused =
        assertThrows(ToolException.class, () -> guarded.require("sourceUrl", url));

    assertTrue(
        refused.getMessage().contains("not on the public internet"),
        "should say why, so the agent stops retrying: " + refused.getMessage());
  }

  // Verifies a scheme that is not HTTP is refused before anything is resolved. file:// would
  // otherwise let an agent read the server's own disk through the upload tool.
  @ParameterizedTest
  @ValueSource(strings = {"file:///etc/passwd", "ftp://example.com/blob", "gopher://example.com"})
  void refusesASchemeThatIsNotHttp(String url) {
    ToolException refused =
        assertThrows(ToolException.class, () -> guarded.require("sourceUrl", url));

    assertTrue(refused.getMessage().contains("http or https"), refused.getMessage());
  }

  // Verifies a public address passes, since a guard that refuses everything is a broken tool
  // rather than a safe one.
  @Test
  void acceptsAPublicAddress() {
    assertEquals(
        "http://93.184.216.34/photo.jpg",
        guarded.require("sourceUrl", "http://93.184.216.34/photo.jpg").toString());
  }

  // Verifies the escape hatch works, which a self-hosted or LAN deployment needs and which the
  // integration test depends on, since Testcontainers publishes on loopback.
  @Test
  void allowsPrivateAddressesWhenTheOperatorAsksFor() {
    PublicHttpUrl permissive = new PublicHttpUrl(true);

    assertEquals(
        "http://127.0.0.1:3000/blob",
        permissive.require("sourceUrl", "http://127.0.0.1:3000/blob").toString());
  }

  // Verifies the refusal names the argument, because an agent given "invalid URL" with no
  // subject will re-send the same call with a different field changed.
  @Test
  void aRefusalNamesTheArgumentItCameFrom() {
    ToolException refused =
        assertThrows(ToolException.class, () -> guarded.require("server", "not a url at all"));

    assertTrue(refused.getMessage().contains("server"), refused.getMessage());
  }
}
