package nostr.mcp.tool;

import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.write.WriteGuard;

import java.util.List;
import java.util.Map;

/**
 * Posts a text note, the thing an agent is usually asked to do.
 *
 * <p>Kind 1 has its own tool rather than being a case of the general one, because the common
 * action should be the easy one: a model asked to "post that" should reach for a tool whose only
 * argument is the text, not compose a raw event and choose a kind number.
 */
public final class PublishNoteTool extends PublishingTool {

  private static final int TEXT_NOTE_KIND = 1;

  /**
   * @param writeGuard the point every write passes through
   */
  public PublishNoteTool(WriteGuard writeGuard) {
    super(writeGuard);
  }

  @Override
  public String name() {
    return "nostr_publish_note";
  }

  @Override
  public String description() {
    return "Publish a public text note to Nostr. Anything published is public and cannot be"
        + " reliably deleted.";
  }

  @Override
  protected Map<String, Object> writeSpecificProperties() {
    return Map.of("content", Map.of("type", "string", "description", "The text of the note."));
  }

  @Override
  protected List<String> writeSpecificRequired() {
    return List.of("content");
  }

  @Override
  protected GenericEvent buildEvent(ToolArguments arguments) {
    return GenericEvent.builder()
        .kind(TEXT_NOTE_KIND)
        .content(arguments.requireText("content"))
        .createdAt(System.currentTimeMillis() / 1000)
        .build();
  }

  @Override
  protected String describeForPreview(GenericEvent event) {
    return event.getContent();
  }
}
