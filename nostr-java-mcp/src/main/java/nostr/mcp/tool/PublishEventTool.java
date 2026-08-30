package nostr.mcp.tool;

import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.write.WriteGuard;

import java.util.List;
import java.util.Map;

/**
 * Publishes an event of any kind: the escape hatch.
 *
 * <p>Nostr's kinds are open-ended and new NIPs arrive continuously, so a server offering only the
 * kinds someone thought to wrap would age badly. This tool lets an agent use a NIP this module
 * has never heard of, at the cost of the caller composing the event itself.
 *
 * <p>It goes through the same guard as every other write. An escape hatch that bypassed
 * confirmation would make the safety model advisory, since anything refused elsewhere could be
 * published here instead.
 */
public final class PublishEventTool extends PublishingTool {

  private static final int MAX_PREVIEW_LENGTH = 500;

  /**
   * @param writeGuard the point every write passes through
   */
  public PublishEventTool(WriteGuard writeGuard) {
    super(writeGuard);
  }

  @Override
  public String name() {
    return "nostr_publish_event";
  }

  @Override
  public String description() {
    return "Publish an event of any kind, for NIPs without a dedicated tool. Anything published"
        + " is public and cannot be reliably deleted.";
  }

  @Override
  protected Map<String, Object> writeSpecificProperties() {
    return Map.of(
        "kind", Map.of("type", "integer", "description", "The NIP-01 event kind."),
        "content", Map.of("type", "string", "description", "The event content."),
        "tags",
            Map.of(
                "type",
                "array",
                "description",
                "Tags as arrays, such as [[\"p\",\"<pubkey>\"],[\"t\",\"nostr\"]].",
                "items", Map.of("type", "array", "items", Map.of("type", "string"))));
  }

  @Override
  protected List<String> writeSpecificRequired() {
    return List.of("kind");
  }

  @Override
  protected GenericEvent buildEvent(ToolArguments arguments) {
    GenericEvent event =
        GenericEvent.builder()
            .kind(
                arguments
                    .integer("kind")
                    .orElseThrow(() -> ToolFailure.INVALID_ARGUMENT.raise("'kind' is required")))
            .content(arguments.text("content").orElse(""))
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    tagsFrom(arguments).forEach(event::addTag);
    return event;
  }

  /**
   * Reads the nested arrays NIP-01 uses for tags.
   *
   * <p>A tag is a list whose first element names it, so a malformed entry is refused by name
   * rather than being published as something the caller did not intend.
   */
  private List<GenericTag> tagsFrom(ToolArguments arguments) {
    return arguments.nestedTexts("tags").stream()
        .map(
            values -> {
              if (values.isEmpty()) {
                throw ToolFailure.INVALID_ARGUMENT.raise(
                    "Every tag needs at least a name, such as [\"p\", \"<pubkey>\"]");
              }
              return new GenericTag(values.getFirst(), values.subList(1, values.size()));
            })
        .toList();
  }

  @Override
  protected String describeForPreview(GenericEvent event) {
    String content = event.getContent() == null ? "" : event.getContent();
    String shown =
        content.length() > MAX_PREVIEW_LENGTH
            ? content.substring(0, MAX_PREVIEW_LENGTH) + "..."
            : content;
    return "kind " + event.getKind() + ", " + event.getTags().size() + " tag(s)\n" + shown;
  }
}
