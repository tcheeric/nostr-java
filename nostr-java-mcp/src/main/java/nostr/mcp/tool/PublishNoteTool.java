package nostr.mcp.tool;

import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.mcp.argument.NostrIdentifier;
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
  private static final String REPLY_TAG = "e";
  private static final String MENTION_TAG = "p";
  private static final String REPLY_MARKER = "reply";

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
    return Map.of(
        "content", Map.of("type", "string", "description", "The text of the note."),
        "replyTo",
            Map.of(
                "type",
                "string",
                "description",
                "The note this replies to, as hex, note or nevent. Omit for a new note."),
        "mentions",
            Map.of(
                "type", "array",
                "description", "Public keys to mention, as hex or npub.",
                "items", Map.of("type", "string")));
  }

  @Override
  protected List<String> writeSpecificRequired() {
    return List.of("content");
  }

  @Override
  protected GenericEvent buildEvent(ToolArguments arguments) {
    GenericEvent note =
        GenericEvent.builder()
            .kind(TEXT_NOTE_KIND)
            .content(arguments.requireText("content"))
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    addThreadingTags(note, arguments);
    return note;
  }

  /**
   * Marks a reply and its mentions the way NIP-10 expects.
   *
   * <p>Without the {@code e} tag a reply is an unrelated note that happens to mention the same
   * subject, and every client will show it detached from the conversation it answers. Mentioned
   * keys get a {@code p} tag, which is how the person mentioned is notified at all.
   */
  private void addThreadingTags(GenericEvent note, ToolArguments arguments) {
    arguments
        .text("replyTo")
        .map(replyTo -> NostrIdentifier.eventId("replyTo", replyTo).hex())
        .ifPresent(
            eventId ->
                note.addTag(new GenericTag(REPLY_TAG, List.of(eventId, "", REPLY_MARKER))));
    arguments.texts("mentions").stream()
        .map(mention -> NostrIdentifier.publicKey("mentions", mention).hex())
        .forEach(pubkey -> note.addTag(new GenericTag(MENTION_TAG, List.of(pubkey))));
  }

  @Override
  protected String describeForPreview(GenericEvent event) {
    return event.getContent();
  }
}
