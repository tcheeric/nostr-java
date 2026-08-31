package nostr.mcp.guidance;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the prompts teach the sequences that models actually get wrong.
 *
 * <p>Each assertion here corresponds to a specific mistake: publishing without confirming,
 * querying the whole network instead of the follow list, and reading "nothing yet" as "nothing
 * matched". A prompt that omits its warning is a prompt that has stopped doing its job.
 */
class NostrPromptsTest {

  private static final Path GUIDE = Path.of("../docs/howto/run-the-mcp-server.md");

  // Verifies the composing prompt names the confirmation step, which is the step a model most
  // often skips and the one that makes a hallucinated post a no-op.
  @Test
  void theComposingPromptNamesTheConfirmationStep() {
    String instructions = instructionsFor("compose-note", Map.of("topic", "anything"));

    assertTrue(instructions.contains("confirmationToken"), instructions);
    assertTrue(instructions.contains("Do not invent"), instructions);
  }

  // Verifies it warns against republishing a partially-accepted note, since retrying a write
  // that already landed is worse on a permanent medium than the incomplete send.
  @Test
  void theComposingPromptWarnsAgainstRepublishing() {
    String instructions = instructionsFor("compose-note", Map.of("topic", "anything"));

    assertTrue(instructions.contains("do not send it again"), instructions);
  }

  // Verifies the feed prompt starts from the follow list, since a model left to itself queries
  // broadly and then filters, which returns strangers and misses the people followed.
  @Test
  void theFeedPromptStartsFromTheFollowList() {
    String instructions = instructionsFor("catch-up-feed", Map.of());

    assertTrue(instructions.indexOf("nostr_get_contacts") < instructions.indexOf("nostr_query_events"),
        "the prompt should read contacts before querying");
    assertTrue(instructions.contains("truncated"), instructions);
    assertTrue(instructions.contains("timedOut"), instructions);
  }

  // Verifies the feed prompt uses the caller's time range, and has a sensible one when none is
  // given, so the tool is usable with no arguments.
  @Test
  void theFeedPromptUsesTheGivenTimeRange() {
    assertTrue(instructionsFor("catch-up-feed", Map.of("since", "7d")).contains("7d"));
    assertTrue(instructionsFor("catch-up-feed", Map.of()).contains("24h"));
  }

  // Verifies the watching prompt distinguishes a replaying backlog from an empty result, which
  // is the specific way an agent reports "nobody mentioned you" when it simply read too early.
  @Test
  void theWatchingPromptDistinguishesReplayingFromEmpty() {
    String instructions = instructionsFor("watch-mentions", Map.of());

    assertTrue(instructions.contains("backlogDrained"), instructions);
    assertTrue(instructions.contains("not \"nothing mentions you\""), instructions);
    assertTrue(instructions.contains("droppedCount"), instructions);
  }

  // Verifies every prompt is described in the guide, so the documented list cannot fall behind
  // the prompts the server actually offers.
  @Test
  void everyPromptIsDocumented() {
    String guide = read(GUIDE);

    for (SyncPromptSpecification prompt : NostrPrompts.all()) {
      assertTrue(
          guide.contains("`" + prompt.prompt().name() + "`"),
          prompt.prompt().name() + " is registered but absent from the guide");
    }
  }

  // Verifies each prompt describes itself, since a host shows the description to a user choosing
  // between them.
  @Test
  void everyPromptDescribesItself() {
    for (SyncPromptSpecification prompt : NostrPrompts.all()) {
      assertFalse(prompt.prompt().description().isBlank(), prompt.prompt().name() + " has no description");
      assertFalse(prompt.prompt().title().isBlank(), prompt.prompt().name() + " has no title");
    }
  }

  // Verifies the prompts are the three the specification names, in a stable order.
  @Test
  void theExpectedPromptsAreOffered() {
    assertEquals(
        List.of("compose-note", "catch-up-feed", "watch-mentions"),
        NostrPrompts.all().stream().map(prompt -> prompt.prompt().name()).toList());
  }

  private String instructionsFor(String name, Map<String, Object> arguments) {
    SyncPromptSpecification prompt =
        NostrPrompts.all().stream()
            .filter(candidate -> candidate.prompt().name().equals(name))
            .findFirst()
            .orElseThrow();
    return prompt
        .promptHandler()
        .apply(null, new GetPromptRequest(name, arguments))
        .messages()
        .stream()
        .map(message -> ((TextContent) message.content()).text())
        .findFirst()
        .orElse("");
  }

  private String read(Path file) {
    try {
      return Files.readString(file);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + file, e);
    }
  }
}
