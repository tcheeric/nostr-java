package nostr.mcp.guidance;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import java.util.List;
import java.util.Map;

/**
 * Teaches a host how to sequence the tools.
 *
 * <p>A tool surface with no guidance makes an agent learn by trial and error, which on a public
 * and irreversible medium is the wrong way to learn: the mistakes are permanent and other people
 * see them. These prompts encode the orderings that work, so the common tasks do not have to be
 * rediscovered by each model in each conversation.
 *
 * <p>Each is written as instructions to the agent rather than as a template the user fills in,
 * because the failure being prevented is the agent choosing a wrong sequence, not the user
 * phrasing a request badly.
 */
public final class NostrPrompts {

  private NostrPrompts() {}

  /**
   * Every prompt this server offers.
   *
   * @return the prompt specifications to register
   */
  public static List<SyncPromptSpecification> all() {
    return List.of(composeNote(), catchUpFeed(), watchMentions());
  }

  /**
   * Composing a note, with the confirmation step made explicit.
   *
   * <p>The step models most often get wrong is treating the preview as the publication, so this
   * says plainly that the first call publishes nothing and that the user should see the text
   * before the token is returned.
   */
  private static SyncPromptSpecification composeNote() {
    Prompt prompt =
        new Prompt(
            "compose-note",
            "Write and publish a Nostr note",
            "Draft a note, show it to the user, and publish it only once they agree.",
            List.of(new PromptArgument("topic", "What the note should be about", true)));

    return new SyncPromptSpecification(
        prompt,
        (exchange, request) ->
            result(
                "Help the user publish a Nostr note about: "
                    + argument(request.arguments(), "topic")
                    + """
                    .

                    Follow this order:

                    1. Draft the note and show the user the exact text. Nostr notes are public \
                    and cannot be reliably deleted, so they must see it before it goes out.
                    2. Call nostr_publish_note with the content. If the server requires \
                    confirmation, this publishes nothing: it returns a preview and a \
                    confirmationToken.
                    3. Show the user what came back and ask whether to go ahead.
                    4. Only if they agree, call nostr_publish_note again with the same content \
                    and the confirmationToken.

                    Do not invent a confirmationToken. If you did not receive one, go back to \
                    step 2. If the result reports that some relays refused the note, it is still \
                    published: do not send it again."""));
  }

  /**
   * Reading a feed, with the follow list as the starting point.
   *
   * <p>Models reach for a broad query and then try to filter it themselves, which returns
   * strangers' notes and misses the people the user actually follows.
   */
  private static SyncPromptSpecification catchUpFeed() {
    Prompt prompt =
        new Prompt(
            "catch-up-feed",
            "Summarise recent notes from the people you follow",
            "Read the user's follow list and summarise what those accounts have posted.",
            List.of(new PromptArgument("since", "How far back to look, such as '24h'", false)));

    return new SyncPromptSpecification(
        prompt,
        (exchange, request) ->
            result(
                """
                Summarise what the people the user follows have posted recently.

                Follow this order:

                1. Call nostr_get_contacts with no arguments to read the user's own follow list.
                2. Call nostr_query_events with those public keys as `authors`, `kinds: [1]`, \
                and `since` set to """
                    + argumentOr(request.arguments(), "since", "24h")
                    + """
                    .
                    3. Summarise the notes by theme rather than listing them one by one.

                    Two things to watch for in the result. If `truncated` is true you did not \
                    see everything, so say the summary is partial or narrow the time range. If \
                    `timedOut` is true the relays did not finish answering, so do not report \
                    "nothing happened" when the truth is that you stopped looking.

                    If the user follows nobody, say so rather than querying every author on the \
                    network."""));
  }

  /**
   * Watching for mentions, with the asynchrony of subscriptions made explicit.
   *
   * <p>The mistake here is reading immediately and concluding nothing matched, when the relays
   * have simply not finished replaying yet.
   */
  private static SyncPromptSpecification watchMentions() {
    Prompt prompt =
        new Prompt(
            "watch-mentions",
            "Watch for new mentions of the user",
            "Open a subscription for mentions and report them as they arrive.",
            List.of());

    return new SyncPromptSpecification(
        prompt,
        (exchange, request) ->
            result(
                """
                Watch for new notes that mention the user.

                Follow this order:

                1. Call nostr_list_identities to find the user's public key.
                2. Call nostr_subscribe with `kinds: [1]` and a `p` tag filter naming that \
                public key, so you receive notes that mention them.
                3. Note the subscriptionId that comes back.
                4. Call nostr_read_subscription with that id whenever you want to check.

                A subscription does not answer immediately. When you first read it the relays \
                may still be replaying their stored events, and the result says so with \
                `backlogDrained`. An empty read with `backlogDrained: false` means "not yet", \
                not "nothing mentions you". Do not report the second when the first is true.

                If a read reports a `droppedCount` above zero, the buffer overflowed and you \
                have missed some mentions: tell the user rather than presenting what you have \
                as the complete picture.

                Call nostr_unsubscribe when the user is done, so the subscription is not left \
                open."""));
  }

  private static GetPromptResult result(String instructions) {
    return new GetPromptResult(
        null, List.of(new PromptMessage(Role.USER, new TextContent(instructions))));
  }

  private static String argument(Map<String, Object> arguments, String name) {
    return argumentOr(arguments, name, "");
  }

  private static String argumentOr(Map<String, Object> arguments, String name, String fallback) {
    Object value = arguments == null ? null : arguments.get(name);
    return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
  }
}
