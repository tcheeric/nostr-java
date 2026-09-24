package nostr.mcp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Drives the tool surface with a real language model.
 *
 * <p>Every other test here asks whether the tools work. This asks the different question the
 * module actually exists to answer: whether a model can <em>use</em> them. A tool can be
 * correct and still unusable, because its name misleads, its description omits the thing the
 * model needs to decide, or its schema invites an argument the model cannot supply. None of
 * that is visible to a test that calls the tool directly, and all of it decides whether the
 * server is any good in the hands of an agent.
 *
 * <p>It runs against a local Ollama, using the host's model cache so no model is downloaded.
 * Where no cache is present the test skips rather than pulling gigabytes, since a test that
 * silently downloads a model on someone's laptop is a test they will disable.
 *
 * <p>Tagged {@code model-driven} and excluded from the ordinary build, because it takes minutes
 * and depends on a model being present. Run it when changing a tool's name, description or
 * schema, since it is the only test that can tell whether a model still understands them:
 *
 * <pre>{@code mvn verify -pl nostr-java-mcp -Dexcluded.it.groups= -Dgroups=model-driven}</pre>
 *
 * <p><strong>What it does and does not prove.</strong> Names and descriptions reinforce each
 * other, so these tests still pass if only one of the two is spoiled: gutting
 * {@code nostr_subscribe}'s description to "Get events." leaves the name carrying the meaning,
 * and the suite stays green. Checked the other way round, with both tools renamed to
 * {@code nostr_alpha} and {@code nostr_beta}, the model still chooses correctly from the
 * descriptions alone, so the descriptions are doing real work rather than riding on the names.
 * Read a pass as "the surface is comprehensible", not as "every word of it is load-bearing".
 */
@Tag("model-driven")
@Testcontainers
class OllamaAgentIT {

  private static final String MODEL = "qwen2.5:7b";
  private static final Path HOST_MODELS = Path.of(System.getProperty("user.home"), ".ollama", "models");
  private static final Duration MODEL_TIMEOUT = Duration.ofMinutes(5);
  private static final ObjectMapper JSON = new ObjectMapper();

  private static final Network NETWORK = Network.newNetwork();

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13"))
          .withExposedPorts(8080)
          .withStartupAttempts(5)
          .withNetwork(NETWORK)
          .waitingFor(new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  @Container
  private static final OllamaContainer OLLAMA =
      new OllamaContainer(DockerImageName.parse("ollama/ollama:latest"))
          // Reuses the models already on this machine. Testcontainers has no preload hook, and
          // pulling several gigabytes inside a test run would make it unusable in practice.
          .withFileSystemBind(HOST_MODELS.toString(), "/root/.ollama/models")
          .withStartupTimeout(Duration.ofMinutes(3));

  @BeforeAll
  static void requireTheModel() {
    assumeTrue(Files.isDirectory(HOST_MODELS), "no local Ollama model cache; skipping");
    assumeTrue(availableModels().contains(MODEL), MODEL + " is not pulled locally; skipping");
  }

  // Verifies a model asked a question in plain language picks the right tool unprompted. This is
  // the claim a tool surface makes and the one nothing else here tests: the names and
  // descriptions have to be good enough that a model reaches for the correct one on its own.
  @Test
  void aModelChoosesTheRightToolForAPlainLanguageQuestion() throws Exception {
    try (McpSyncClient mcp = launchServer()) {
      mcp.initialize();

      JsonNode call = firstToolCall(ask("Which relays is this Nostr server connected to?", toolsOf(mcp)));

      assertEquals("nostr_list_relays", call.path("function").path("name").asText(), call.toString());
    }
  }

  // Verifies the model can carry a tool's answer back into a useful reply, which is the whole
  // round trip an agent performs and the reason the results are worded for a reader.
  @Test
  void aModelCanUseAToolResultToAnswerTheQuestion() throws Exception {
    try (McpSyncClient mcp = launchServer()) {
      mcp.initialize();
      List<Tool> tools = toolsOf(mcp);

      JsonNode call = firstToolCall(ask("Which relays is this Nostr server connected to?", tools));
      CallToolResult result = mcp.callTool(new CallToolRequest(call.path("function").path("name").asText(), Map.of()));
      String answer = answerAfterToolResult("Which relays is this server connected to?", tools, call, textOf(result));

      assertTrue(answer.toLowerCase().contains("relay"), answer);
    }
  }

  // Verifies the model reaches for the querying tool, not the subscribing one, when asked about
  // things that already happened. The two are easy to confuse and the distinction lives entirely
  // in their descriptions, so this is really a test of how they are worded.
  @Test
  void aModelDistinguishesQueryingFromSubscribing() throws Exception {
    try (McpSyncClient mcp = launchServer()) {
      mcp.initialize();
      List<Tool> tools = toolsOf(mcp);

      String past = firstToolCall(ask("What notes have been posted recently? Look at what already exists.", tools))
          .path("function").path("name").asText();
      String future = firstToolCall(ask("Start watching for any new notes that arrive from now on.", tools))
          .path("function").path("name").asText();

      assertEquals("nostr_query_events", past, "asked about the past, the model chose " + past);
      assertEquals("nostr_subscribe", future, "asked to watch, the model chose " + future);
    }
  }

  // Verifies a model asked to post produces a publish call rather than something destructive.
  // Publishing is irreversible, so the surface must not make a neighbouring tool look plausible.
  @Test
  void aModelAskedToPostChoosesToPublish() throws Exception {
    try (McpSyncClient mcp = launchServer()) {
      mcp.initialize();

      JsonNode call = firstToolCall(ask("Post a note saying hello to Nostr.", toolsOf(mcp)));
      String chosen = call.path("function").path("name").asText();

      assertEquals("nostr_publish_note", chosen, "the model chose " + chosen);
      assertTrue(call.path("function").path("arguments").has("content"), call.toString());
    }
  }

  // Verifies the confirmation preview reads as "not yet done" to a model, which is the property
  // the whole write guard depends on. A model that reads the preview as success would tell its
  // user the note is published and never send the token.
  @Test
  void aModelUnderstandsThatAPreviewHasNotPublishedYet() throws Exception {
    try (McpSyncClient mcp = launchServer()) {
      mcp.initialize();

      CallToolResult preview =
          mcp.callTool(new CallToolRequest("nostr_publish_note", Map.of("content", "hello from a test")));
      String verdict =
          askPlainly(
              "A tool returned this exactly:\n\n"
                  + textOf(preview)
                  + "\n\nHas the note been published yet? Answer with the single word YES or NO.");

      assertTrue(verdict.toUpperCase().contains("NO"), "the model read the preview as published: " + verdict);
    }
  }

  // Verifies a model reads an empty subscription that is still replaying as "not yet", not as
  // "there is nothing". That distinction is why the result carries backlogDrained at all, and it
  // only matters if a model actually acts on it.
  @Test
  void aModelUnderstandsAReplayingSubscriptionIsNotAnEmptyOne() throws Exception {
    String verdict =
        askPlainly(
            "A Nostr tool returned this exactly:\n\n"
                + "Nothing yet: the relays are still replaying their stored events.\n\n"
                + "Does this mean there are definitely no matching events? Answer YES or NO.");

    assertTrue(verdict.toUpperCase().contains("NO"), "the model treated a replaying read as empty: " + verdict);
  }

  // Verifies the model reaches the right tool across the whole surface, not just the handful a
  // few hand-written cases happen to cover. Each request is phrased as a user would put it, with
  // every one of the twenty-two tools offered, so a tool whose name or description does not
  // distinguish it from its neighbours shows up here.
  @ParameterizedTest(name = "\"{0}\" should reach {1}")
  @MethodSource("requestsAndTheToolTheyNeed")
  void aModelReachesTheRightToolAcrossTheSurface(String request, String expectedTool) throws Exception {
    try (McpSyncClient mcp = launchServer()) {
      mcp.initialize();

      JsonNode call = firstToolCall(ask(request, toolsOf(mcp)));

      assertEquals(expectedTool, call.path("function").path("name").asText(), call.toString());
    }
  }

  /**
   * One plain-language request per tool an agent would plausibly be asked to reach.
   *
   * <p>Covers the surface rather than a sample, because the risk being tested is that two tools
   * read alike to a model, and that only shows when both are on offer. The identity lifecycle
   * tools are included deliberately: they neighbour each other closely, and choosing "remove"
   * where "rename" was meant is not recoverable.
   */
  private static Stream<Arguments> requestsAndTheToolTheyNeed() {
    return Stream.of(
        arguments("Which relays is this server connected to?", "nostr_list_relays"),
        arguments("What is the name and description of the relay wss://relay.example?", "nostr_relay_info"),
        arguments("Which identities can this server sign as?", "nostr_list_identities"),
        arguments("Find notes posted in the last day.", "nostr_query_events"),
        arguments("Look up the profile for npub1abc, what is their bio?", "nostr_get_profile"),
        arguments("Show me the replies to note1xyz so I can read the conversation.", "nostr_fetch_thread"),
        arguments("Who do I follow?", "nostr_get_contacts"),
        // Deliberately not "mentioning me": that needs the caller's own key, and a model that
        // asks which identity to watch rather than guessing is behaving correctly. Testing tool
        // selection means not conflating it with a missing argument the model is right to
        // question.
        arguments("Start watching for any new notes of kind 1 as they arrive.", "nostr_subscribe"),
        arguments("Any new events in my watch with id sub-1 yet?", "nostr_read_subscription"),
        arguments("What am I currently watching?", "nostr_list_subscriptions"),
        arguments("Stop watching subscription sub-1.", "nostr_unsubscribe"),
        arguments("Post a note saying hello to Nostr.", "nostr_publish_note"),
        arguments("Change my display name to Alice and my bio to 'testing'.", "nostr_update_profile"),
        arguments("Send a private encrypted message to npub1abc saying hi.", "nostr_send_direct_message"),
        arguments("Do I have any private messages?", "nostr_read_direct_messages"),
        arguments("Make me a brand new Nostr account called project-bot.", "nostr_create_identity"),
        arguments("I have an existing Nostr key saved in the file /tmp/key.txt. Add it to this"
                + " server under the alias 'adopted'.", "nostr_import_identity"),
        arguments("Rename my identity 'old-name' to 'new-name'.", "nostr_rename_identity"),
        arguments("From now on sign as 'project-bot' by default.", "nostr_set_default_identity"),
        arguments("Save an encrypted backup of my key 'personal' to /tmp/backup.p12.", "nostr_export_identity_backup"),
        // The Blossom tools sit close to each other and closer still to the Nostr ones: a model
        // asked to "share this picture" could reasonably reach for a publishing tool instead, so
        // each is phrased the way someone would actually ask rather than by naming the protocol.
        arguments("Upload the image at https://example.com/cat.png to my media server and give"
                + " me the link.", "nostr_blossom_upload"),
        arguments("Where can I download the file with hash"
                + " b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553?",
            "nostr_blossom_get"),
        arguments("What files have I uploaded to my media server?", "nostr_blossom_list"),
        arguments("Remove the file"
                + " b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553 from my"
                + " media server.", "nostr_blossom_delete"),
        arguments("Which media servers does npub1abc use for their files?",
            "nostr_blossom_get_servers"),
        arguments("Tell the network that I host my media on https://cdn.example.com.",
            "nostr_blossom_set_servers"));
  }

  private List<Tool> toolsOf(McpSyncClient mcp) {
    return mcp.listTools().tools();
  }

  /** Asks the model a question with the real tool surface attached. */
  private JsonNode ask(String question, List<Tool> tools) throws Exception {
    ObjectNode request = JSON.createObjectNode();
    request.put("model", MODEL);
    request.put("stream", false);
    ArrayNode messages = request.putArray("messages");
    messages.addObject().put("role", "user").put("content", question);
    request.set("tools", asOllamaTools(tools));
    return chat(request);
  }

  /** Asks the model to reason about a tool's output, with no tools attached. */
  private String askPlainly(String question) throws Exception {
    ObjectNode request = JSON.createObjectNode();
    request.put("model", MODEL);
    request.put("stream", false);
    request.putArray("messages").addObject().put("role", "user").put("content", question);
    return chat(request).path("content").asText();
  }

  private String answerAfterToolResult(
      String question, List<Tool> tools, JsonNode call, String toolOutput) throws Exception {
    ObjectNode request = JSON.createObjectNode();
    request.put("model", MODEL);
    request.put("stream", false);
    ArrayNode messages = request.putArray("messages");
    messages.addObject().put("role", "user").put("content", question);
    ObjectNode assistant = messages.addObject();
    assistant.put("role", "assistant").put("content", "");
    assistant.putArray("tool_calls").add(call);
    messages.addObject().put("role", "tool").put("content", toolOutput);
    request.set("tools", asOllamaTools(tools));
    return chat(request).path("content").asText();
  }

  /**
   * Translates the MCP tool surface into Ollama's function-calling shape.
   *
   * <p>Deliberately a direct mapping of what the server advertises: the point is to test the
   * real names, descriptions and schemas, so anything reworded here would be testing this
   * method instead of the server.
   */
  private ArrayNode asOllamaTools(List<Tool> tools) {
    ArrayNode array = JSON.createArrayNode();
    for (Tool tool : tools) {
      ObjectNode entry = array.addObject();
      entry.put("type", "function");
      ObjectNode function = entry.putObject("function");
      function.put("name", tool.name());
      function.put("description", tool.description());
      function.set("parameters", JSON.valueToTree(tool.inputSchema()));
    }
    return array;
  }

  private JsonNode chat(ObjectNode request) throws Exception {
    HttpResponse<String> response =
        HttpClient.newBuilder()
            .connectTimeout(MODEL_TIMEOUT)
            .build()
            .send(
                HttpRequest.newBuilder(URI.create(OLLAMA.getEndpoint() + "/api/chat"))
                    .timeout(MODEL_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                    .build(),
                HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode(), response.body());
    return JSON.readTree(response.body()).path("message");
  }

  private JsonNode firstToolCall(JsonNode message) {
    JsonNode calls = message.path("tool_calls");
    assertTrue(calls.isArray() && !calls.isEmpty(),
        "the model called no tool; it replied: " + message.path("content").asText());
    return calls.get(0);
  }

  private static List<String> availableModels() {
    try {
      HttpResponse<String> response =
          HttpClient.newHttpClient()
              .send(
                  HttpRequest.newBuilder(URI.create(OLLAMA.getEndpoint() + "/api/tags"))
                      .timeout(Duration.ofSeconds(30))
                      .GET()
                      .build(),
                  HttpResponse.BodyHandlers.ofString());
      List<String> names = new ArrayList<>();
      JSON.readTree(response.body()).path("models").forEach(model -> names.add(model.path("name").asText()));
      return names;
    } catch (Exception e) {
      return List.of();
    }
  }

  /** Launches the server exactly as an MCP host does, pointed at the relay. */
  private McpSyncClient launchServer() {
    ServerParameters parameters =
        ServerParameters.builder("java")
            .args(
                "-Dnostr.mcp.relays.read=ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(8080),
                "-cp",
                System.getProperty("java.class.path"),
                nostr.mcp.NostrMcpApplication.class.getName())
            .build();
    return McpClient.sync(new StdioClientTransport(parameters, McpJsonDefaults.getMapper()))
        .requestTimeout(Duration.ofSeconds(60))
        .build();
  }

  private String textOf(CallToolResult result) {
    return result.content().stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .map(TextContent::text)
        .findFirst()
        .orElse("");
  }
}
