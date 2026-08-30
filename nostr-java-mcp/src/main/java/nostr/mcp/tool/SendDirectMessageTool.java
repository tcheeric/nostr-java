package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.api.RecipientDeliveryOutcome;
import nostr.base.PublicKey;
import nostr.mcp.argument.NostrIdentifier;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.social.McpDirectMessageService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends a private message that only its recipients can read.
 *
 * <p>NIP-17 gift wrapping means the relay sees neither the correspondents nor the content, and
 * delivery goes only to the relays each recipient nominated. Two consequences have to reach the
 * agent rather than being smoothed over.
 *
 * <p>First, a recipient who has published no relay list cannot be sent to at all, so the message
 * genuinely did not arrive and the user must be told who missed it. Second, every conversation
 * includes a copy addressed to the sender, so a message to one person produces two outcomes.
 * Reporting that as "1 of 2 delivered" would tell a user their message failed when it arrived
 * perfectly well, so the sender's archival copy is reported separately from the recipients.
 */
public final class SendDirectMessageTool implements NostrTool {

  private final McpDirectMessageService directMessages;
  private final IdentityVault identityVault;

  /**
   * @param directMessages composes and delivers the message
   * @param identityVault resolves which identity to send as
   */
  public SendDirectMessageTool(
      @NonNull McpDirectMessageService directMessages, @NonNull IdentityVault identityVault) {
    this.directMessages = directMessages;
    this.identityVault = identityVault;
  }

  @Override
  public String name() {
    return "nostr_send_direct_message";
  }

  @Override
  public String description() {
    return "Send a private, encrypted direct message (NIP-17). Relays cannot see the sender,"
        + " the recipients or the content.";
  }

  /**
   * A bound server omits {@code identity}, since there is only one sender it could mean.
   */
  @Override
  public Map<String, Object> inputSchema() {
    Map<String, Object> properties = new java.util.LinkedHashMap<>();
    properties.put(
        "recipients",
        Map.of(
            "type", "array",
            "description", "Who to send to, as hex public keys or npubs.",
            "items", Map.of("type", "string")));
    properties.put("content", Map.of("type", "string", "description", "The message text."));
    if (!identityVault.binding().isBound()) {
      properties.put(
          "identity", Map.of("type", "string", "description", "Alias to send as. Omit for the default."));
    }
    return Map.of("type", "object", "properties", properties, "required", List.of("recipients", "content"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return send(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult send(ToolArguments arguments) {
    String alias = resolveIdentity(arguments);
    List<PublicKey> recipients = recipientsFrom(arguments);
    List<RecipientDeliveryOutcome> outcomes =
        directMessages.send(alias, recipients, arguments.requireText("content"));

    String senderKey = directMessages.publicKeyOf(alias).toHexString();
    List<RecipientDeliveryOutcome> toRecipients =
        outcomes.stream().filter(outcome -> !outcome.recipient().equals(senderKey)).toList();
    List<RecipientDeliveryOutcome> ownCopy =
        outcomes.stream().filter(outcome -> outcome.recipient().equals(senderKey)).toList();

    return CallToolResult.builder()
        .structuredContent(structured(toRecipients, ownCopy))
        .addTextContent(summarise(toRecipients, ownCopy))
        .build();
  }

  private Map<String, Object> structured(
      List<RecipientDeliveryOutcome> toRecipients, List<RecipientDeliveryOutcome> ownCopy) {
    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("recipients", toRecipients.stream().map(SendDirectMessageTool::describe).toList());
    structured.put("delivered", toRecipients.stream().filter(RecipientDeliveryOutcome::isDelivered).count());
    structured.put("recipientCount", toRecipients.size());
    structured.put(
        "ownArchivalCopy",
        ownCopy.stream().map(SendDirectMessageTool::describe).findFirst().orElse(Map.of()));
    return structured;
  }

  private static Map<String, Object> describe(RecipientDeliveryOutcome outcome) {
    Map<String, Object> described = new LinkedHashMap<>();
    described.put("recipient", outcome.recipient());
    described.put("status", outcome.status().name());
    described.put("reason", outcome.findReason().orElse(""));
    return described;
  }

  /**
   * Reports the recipients as the answer, and the sender's own copy as advice.
   *
   * <p>A sender with no relay list of their own gets an UNREACHABLE archival copy while the real
   * recipient is delivered. That is worth mentioning, because they will not see this message on
   * their other devices, but it is not a delivery failure and must not read as one.
   */
  private String summarise(
      List<RecipientDeliveryOutcome> toRecipients, List<RecipientDeliveryOutcome> ownCopy) {
    long delivered = toRecipients.stream().filter(RecipientDeliveryOutcome::isDelivered).count();
    StringBuilder summary = new StringBuilder();
    if (delivered == toRecipients.size()) {
      summary.append("Delivered to ").append(delivered).append(toRecipients.size() == 1 ? " recipient." : " recipients.");
    } else {
      summary.append("Delivered to ").append(delivered).append(" of ").append(toRecipients.size()).append(" recipients.");
      toRecipients.stream()
          .filter(outcome -> !outcome.isDelivered())
          .forEach(
              outcome ->
                  summary
                      .append(" ")
                      .append(outcome.recipient())
                      .append(" could not be reached")
                      .append(
                          outcome.status() == RecipientDeliveryOutcome.Status.UNREACHABLE
                              ? " because they have published no relay list saying where to send"
                                  + " private messages"
                              : "")
                      .append('.'));
    }
    ownCopy.stream()
        .filter(outcome -> !outcome.isDelivered())
        .findFirst()
        .ifPresent(
            outcome ->
                summary.append(
                    " Your own archival copy was not stored, so this message will not appear in"
                        + " your other clients; publish a kind-10050 relay list to fix that."));
    return summary.toString();
  }

  private List<PublicKey> recipientsFrom(ToolArguments arguments) {
    List<String> recipients = arguments.texts("recipients");
    if (recipients.isEmpty()) {
      throw ToolFailure.INVALID_ARGUMENT.raise("Give at least one recipient");
    }
    return recipients.stream()
        .map(recipient -> NostrIdentifier.publicKey("recipients", recipient).asPublicKey())
        .toList();
  }

  private String resolveIdentity(ToolArguments arguments) {
    return arguments
        .text("identity")
        .orElseGet(
            () ->
                identityVault
                    .defaultAlias()
                    .orElseThrow(
                        () ->
                            ToolFailure.IDENTITY_AMBIGUOUS.raise(
                                "This server holds several identities and none is the default, so"
                                    + " it cannot tell who should send. Name one in 'identity'.")));
  }
}
