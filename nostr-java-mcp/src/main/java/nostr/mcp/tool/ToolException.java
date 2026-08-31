package nostr.mcp.tool;

import lombok.Getter;
import lombok.NonNull;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * A failure raised where it is detected and reported where the agent can see it.
 *
 * <p>Argument decoding and relay access happen several calls below the tool method, so returning
 * a {@link ToolFailure} from there would mean threading a result type through every intermediate
 * signature. Throwing keeps those methods returning the value they are about, and
 * {@link #asResult()} turns the failure back into the agent-facing form at the boundary.
 *
 * <p>Unchecked, because a caller that cannot decode an identifier has nothing useful to do with
 * a checked exception except rethrow it.
 */
@Getter
public final class ToolException extends RuntimeException {

  private final transient ToolFailure failure;

  /**
   * @param failure the stable code the agent will see
   * @param detail what went wrong, in terms the agent can act on
   */
  public ToolException(@NonNull ToolFailure failure, @NonNull String detail) {
    super(detail);
    this.failure = failure;
  }

  /**
   * @param failure the stable code the agent will see
   * @param detail what went wrong
   * @param cause the underlying failure, kept for the server's logs but never shown to the agent
   */
  public ToolException(@NonNull ToolFailure failure, @NonNull String detail, Throwable cause) {
    super(detail, cause);
    this.failure = failure;
  }

  /**
   * Render this failure as the agent sees it.
   *
   * @return an error result carrying the code and the message
   */
  public CallToolResult asResult() {
    return failure.with(getMessage());
  }
}
