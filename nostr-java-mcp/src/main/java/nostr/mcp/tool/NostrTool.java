package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

import java.util.Map;

/**
 * One capability an agent can invoke.
 *
 * <p>A tool owns its own name, schema and behaviour, so adding one means adding a class rather
 * than editing a dispatcher. That matters more here than it usually would: the tool surface is
 * what an agent sees and reasons about, and a registry that grows by accretion is one where a
 * tool can be added without anyone deciding whether an agent should have it.
 *
 * <p>Implementations must not throw for ordinary failures. An agent cannot act on a stack trace,
 * so a tool reports trouble as a result carrying a stable code, which is what
 * {@link ToolFailure} produces.
 */
public interface NostrTool {

  /**
   * The name an agent calls, namespaced {@code nostr_*} so it reads clearly in a tool list.
   *
   * @return the tool name
   */
  String name();

  /**
   * What this tool does, written for a model deciding whether to call it.
   *
   * @return a one-line description
   */
  String description();

  /**
   * The JSON Schema for this tool's arguments.
   *
   * @return the schema, empty when the tool takes none
   */
  Map<String, Object> inputSchema();

  /**
   * Run the tool.
   *
   * @param request the agent's call, carrying its arguments
   * @return the result, structured for a program and summarised for a reader
   */
  CallToolResult call(CallToolRequest request);
}
