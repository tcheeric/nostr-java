package nostr.mcp.tool;

import lombok.NonNull;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.blossom.BlossomServers;
import nostr.mcp.write.WriteGuard;

import java.util.List;
import java.util.Map;

/**
 * Publishes which Blossom servers this identity uses.
 *
 * <p>A plain publishing tool, so it inherits confirmation, the rate limit and identity
 * resolution from {@link nostr.mcp.tool.PublishingTool} rather than restating them. Kind 10063
 * is replaceable: publishing a list replaces the previous one outright, so the call carries
 * every server the user wants listed, not just the new one.
 *
 * <p>Order is preserved because BUD-03 makes it meaningful — clients upload to the first server
 * and search the rest in turn, so the order is the user's statement about which they trust.
 */
public final class BlossomSetServersTool extends PublishingTool {

  private static final String SERVER_TAG = "server";

  private final BlossomServers servers;

  /**
   * @param writeGuard the point every write passes through
   * @param servers validates each URL before it is published
   */
  public BlossomSetServersTool(@NonNull WriteGuard writeGuard, @NonNull BlossomServers servers) {
    super(writeGuard);
    this.servers = servers;
  }

  @Override
  public String name() {
    return "nostr_blossom_set_servers";
  }

  @Override
  public String description() {
    return "Publish the list of Blossom media servers this identity uses (BUD-03, kind 10063),"
        + " most trusted first. Replaces any previous list.";
  }

  @Override
  protected Map<String, Object> writeSpecificProperties() {
    return Map.of(
        "servers",
        Map.of(
            "type", "array",
            "description",
                "Full server URLs including https://, most trusted first. This replaces the"
                    + " existing list, so include every server that should remain.",
            "items", Map.of("type", "string")));
  }

  @Override
  protected List<String> writeSpecificRequired() {
    return List.of("servers");
  }

  @Override
  protected GenericEvent buildEvent(ToolArguments arguments) {
    List<String> urls = arguments.texts("servers");
    if (urls.isEmpty()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'servers' needs at least one server URL. To stop advertising any server, the list"
              + " event has to be deleted rather than published empty.");
    }
    List<BaseTag> tags =
        BlossomServers.fromServerTags(urls).stream()
            .peek(BlossomServers::requireUsable)
            .map(url -> BaseTag.create(SERVER_TAG, url))
            .toList();
    return GenericEvent.builder()
        .kind(BlossomServerListTool.SERVER_LIST_KIND)
        .content("")
        .createdAt(System.currentTimeMillis() / 1000)
        .tags(tags)
        .build();
  }

  @Override
  protected String describeForPreview(GenericEvent event) {
    return "Blossom servers, most trusted first:\n"
        + event.getTags().stream()
            .filter(tag -> SERVER_TAG.equals(tag.getCode()))
            .map(tag -> "  " + ((nostr.event.tag.GenericTag) tag).getParams().get(0))
            .reduce((first, second) -> first + "\n" + second)
            .orElse("  (none)");
  }
}
