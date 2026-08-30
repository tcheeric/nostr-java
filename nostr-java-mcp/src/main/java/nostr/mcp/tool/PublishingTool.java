package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.client.relay.PublishResult;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.write.PendingWrite;
import nostr.mcp.write.WriteGuard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The shape every publishing tool shares: describe an event, then let the guard publish it.
 *
 * <p>Confirmation, rate limiting, identity resolution and result rendering are identical for
 * every write, and only the event differs. A subclass therefore says what to publish and nothing
 * about how, which is what keeps a new write tool from quietly omitting a guard.
 */
public abstract class PublishingTool implements NostrTool {

  private static final String CONFIRMATION_ARGUMENT = "confirmationToken";

  private final WriteGuard writeGuard;

  /**
   * @param writeGuard the single point every write passes through
   */
  protected PublishingTool(@NonNull WriteGuard writeGuard) {
    this.writeGuard = writeGuard;
  }

  /**
   * Build the event this tool publishes.
   *
   * @param arguments the caller's arguments
   * @return the unsigned event, which the guard signs
   */
  protected abstract GenericEvent buildEvent(ToolArguments arguments);

  /**
   * Describe the event for the agent to confirm.
   *
   * @param event the signed event
   * @return a human-readable preview
   */
  protected abstract String describeForPreview(GenericEvent event);

  /**
   * The arguments this tool takes, beyond the ones every write shares.
   *
   * @return the tool-specific schema properties
   */
  protected abstract Map<String, Object> writeSpecificProperties();

  /**
   * The arguments this tool requires when publishing for the first time.
   *
   * @return the required argument names
   */
  protected abstract List<String> writeSpecificRequired();

  @Override
  public final Map<String, Object> inputSchema() {
    Map<String, Object> properties = new LinkedHashMap<>(writeSpecificProperties());
    properties.put("identity", Map.of("type", "string", "description", "Alias to publish as. Omit to use the default."));
    if (writeGuard.requiresConfirmation()) {
      properties.put(
          CONFIRMATION_ARGUMENT,
          Map.of(
              "type",
              "string",
              "description",
              "Token from a previous preview. Omit to preview; supply it to publish."));
    }
    return Map.of("type", "object", "properties", properties, "required", List.of());
  }

  @Override
  public final CallToolResult call(CallToolRequest request) {
    try {
      return publishOrPreview(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  /**
   * Runs whichever half of the conversation the caller is in.
   *
   * <p>The token decides. Its presence means the agent has already seen a preview and is asking
   * to send that exact event, so the arguments are not read again: re-reading them would let the
   * content change between what was shown and what is published.
   */
  private CallToolResult publishOrPreview(ToolArguments arguments) {
    Optional<String> token = arguments.text(CONFIRMATION_ARGUMENT);
    if (token.isPresent()) {
      return published(writeGuard.publishConfirmed(token.get()));
    }
    PendingWrite pending = writeGuard.prepare(buildEvent(arguments), arguments.text("identity"));
    return writeGuard.requiresConfirmation()
        ? preview(pending)
        : published(writeGuard.publishDirectly(pending));
  }

  private CallToolResult preview(PendingWrite pending) {
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "status", "awaiting-confirmation",
                CONFIRMATION_ARGUMENT, pending.token(),
                "identity", pending.identityAlias(),
                "eventId", String.valueOf(pending.event().getId()),
                "kind", pending.event().getKind()))
        .addTextContent(
            "Nothing has been published yet. This would post as '"
                + pending.identityAlias()
                + "':\n\n"
                + describeForPreview(pending.event())
                + "\n\nPublishing to Nostr is public and cannot be reliably undone. To go ahead,"
                + " call this tool again with "
                + CONFIRMATION_ARGUMENT
                + "='"
                + pending.token()
                + "'.")
        .build();
  }

  /**
   * Reports the per-relay outcome, treating any acceptance as success.
   *
   * <p>An event accepted by one relay is on the network. Calling a partial success a failure
   * would invite the agent to retry a write that already landed, and on a permanent medium a
   * duplicate is worse than an incomplete send.
   */
  private CallToolResult published(PublishResult result) {
    List<String> accepted = result.getAcceptingRelays();
    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("eventId", result.getEventId());
    structured.put("acceptedBy", accepted);
    structured.put(
        "failures",
        result.getFailures().stream()
            .map(
                failure ->
                    Map.of(
                        "relay", failure.relayUri(),
                        "status", failure.status().name(),
                        "reason", failure.findReason().orElse("")))
            .toList());
    return CallToolResult.builder()
        .structuredContent(structured)
        .addTextContent(summarise(result, accepted))
        .build();
  }

  private String summarise(PublishResult result, List<String> accepted) {
    StringBuilder summary =
        new StringBuilder("Published ").append(result.getEventId()).append(" to ").append(accepted.size());
    summary.append(accepted.size() == 1 ? " relay" : " relays");
    if (!result.getFailures().isEmpty()) {
      summary.append(". It did not reach ");
      summary.append(result.getFailures().stream().map(failure -> failure.relayUri()).toList());
      summary.append(", but it is published and should not be sent again");
    }
    return summary.append('.').toString();
  }
}
