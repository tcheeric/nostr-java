package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;

/**
 * The stable codes a tool reports instead of throwing.
 *
 * <p>An agent cannot act on a stack trace, but it can act on a code: {@code RELAY_UNREACHABLE}
 * invites a retry, {@code INVALID_ARGUMENT} invites a correction, {@code WRITE_FORBIDDEN} says
 * to stop asking. Keeping the vocabulary in one enum means a new tool picks from the set rather
 * than inventing a phrasing the agent has never seen.
 */
public enum ToolFailure {
  /** An argument was missing, malformed, or outside the allowed range. */
  INVALID_ARGUMENT,
  /** No relay could be reached to answer the request. */
  RELAY_UNREACHABLE,
  /** Every relay refused the event, with their reasons in the message. */
  RELAY_REJECTED,
  /** The relay accepted the request but did not answer in time. */
  TIMEOUT,
  /** The named identity is not in the keystore. */
  IDENTITY_UNKNOWN,
  /** Several identities exist and none is the default, so signing would be a guess. */
  IDENTITY_AMBIGUOUS,
  /** The configured policy does not permit this write. */
  WRITE_FORBIDDEN,
  /** The named subscription does not exist or has been reaped. */
  SUBSCRIPTION_UNKNOWN,
  /** The server already holds as many subscriptions as it allows. */
  SUBSCRIPTION_LIMIT_REACHED;

  /**
   * Raise this failure from wherever it is detected.
   *
   * @param detail what went wrong, in terms the agent can act on
   * @return an exception the tool boundary turns back into a result
   */
  public ToolException raise(@NonNull String detail) {
    return new ToolException(this, detail);
  }

  /**
   * Report this failure to the agent.
   *
   * @param detail what went wrong, in terms the agent can act on
   * @return an error result carrying the code and the detail
   */
  public CallToolResult with(@NonNull String detail) {
    return CallToolResult.builder().isError(true).addTextContent(name() + ": " + detail).build();
  }
}
