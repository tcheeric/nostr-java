package nostr.mcp.blossom;

import nostr.mcp.tool.ToolException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Which server a call acts on, and which URLs an agent is allowed to name. */
class BlossomServersTest {

  private final BlossomServers servers =
      new BlossomServers(
          List.of("https://cdn.example.com/", "https://backup.example.com"),
          new PublicHttpUrl(false));

  // Verifies a call that names no server gets the first configured one, since the operator
  // ordered them by preference and an agent should not have to know any URL to upload.
  @Test
  void aCallThatNamesNoServerUsesTheFirstConfiguredOne() {
    assertEquals("https://cdn.example.com", servers.resolve(Optional.empty()));
  }

  // Verifies the trailing slash is dropped, or every path built from it grows a double slash
  // and two spellings of one server become two servers.
  @Test
  void aTrailingSlashIsNotPartOfTheServer() {
    assertEquals("https://cdn.example.com", servers.resolve(Optional.of("https://cdn.example.com/")));
    assertEquals("https://cdn.example.com/b167", BlossomServers.blobUrl("https://cdn.example.com/", "b167"));
  }

  // Verifies a server the agent names still has to be a public address. The server argument is
  // as much an agent-supplied URL as the source is, and so is as much a way onto this network.
  @Test
  void aServerTheAgentNamesIsStillChecked() {
    ToolException refused =
        assertThrows(
            ToolException.class, () -> servers.resolve(Optional.of("http://169.254.169.254")));

    assertTrue(refused.getMessage().contains("not on the public internet"), refused.getMessage());
  }

  // Verifies a configured server bypasses the check, so an operator may point this at a server
  // on their own network without also opening that network to the agent's choosing.
  @Test
  void aConfiguredServerIsNotSubjectToTheCheck() {
    BlossomServers onLan =
        new BlossomServers(List.of("http://192.168.1.50:3000"), new PublicHttpUrl(false));

    assertEquals("http://192.168.1.50:3000", onLan.resolve(Optional.empty()));
    assertEquals(
        "http://192.168.1.50:3000", onLan.resolve(Optional.of("http://192.168.1.50:3000")));
  }

  // Verifies an unconfigured server with nothing named says what to do about it, rather than
  // failing with a null the agent cannot interpret.
  @Test
  void withNothingConfiguredAndNothingNamedTheRefusalSaysWhatToDo() {
    BlossomServers none = new BlossomServers(List.of(), new PublicHttpUrl(false));

    ToolException refused =
        assertThrows(ToolException.class, () -> none.resolve(Optional.empty()));

    assertTrue(refused.getMessage().contains("nostr.mcp.blossom.servers"), refused.getMessage());
  }

  // Verifies looking for a blob tries every configured server, since the point of a
  // hash-addressed network is that the same blob may be on any of them.
  @Test
  void searchingTriesEveryConfiguredServer() {
    assertEquals(
        List.of("https://cdn.example.com", "https://backup.example.com"),
        servers.resolveAll(Optional.empty()));
  }

  // Verifies publishing a server list checks syntax, not reachability. This event is never
  // fetched by this server, so there is no request to forge; applying the address check here
  // would stop an operator advertising a Blossom server on their own network, and would fail a
  // whole publish on a transient DNS failure for one URL.
  @Test
  void publishingAServerListAcceptsAPrivateAddress() {
    assertEquals(
        "http://blossom.lan:3000",
        BlossomServers.requireUsable("http://blossom.lan:3000/").toString());
    assertEquals(
        "http://192.168.1.50:3000", BlossomServers.requireUsable("http://192.168.1.50:3000").toString());
  }

  // Verifies it still refuses something no client could use, since the point of publishing the
  // list is that other people can read it and fetch from it.
  @Test
  void publishingAServerListRefusesSomethingUnusable() {
    assertThrows(ToolException.class, () -> BlossomServers.requireUsable("not a url"));
    assertThrows(ToolException.class, () -> BlossomServers.requireUsable("ftp://example.com"));
    assertThrows(ToolException.class, () -> BlossomServers.requireUsable("https://"));
  }

  // Verifies a BUD-03 list keeps its order, which the spec makes significant: the author lists
  // their most trusted server first and clients are expected to honour that.
  @Test
  void aPublishedServerListKeepsItsOrderAndDropsDuplicates() {
    assertEquals(
        List.of("https://first.example.com", "https://second.example.com"),
        BlossomServers.fromServerTags(
            List.of(
                "https://first.example.com",
                "https://second.example.com/",
                "https://first.example.com",
                "")));
  }
}
