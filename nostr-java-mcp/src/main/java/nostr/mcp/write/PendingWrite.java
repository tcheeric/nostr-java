package nostr.mcp.write;

import nostr.event.impl.GenericEvent;

/**
 * A signed event waiting for the agent to confirm it.
 *
 * <p>The event is signed before it is previewed, so what the agent confirms is exactly what will
 * be published: signing afterwards would let the content drift between the preview and the send,
 * which is precisely the substitution confirmation exists to prevent.
 *
 * @param token the opaque handle the agent returns to publish this event
 * @param event the signed event, ready to send
 * @param identityAlias who it will be published as
 */
public record PendingWrite(String token, GenericEvent event, String identityAlias) {}
