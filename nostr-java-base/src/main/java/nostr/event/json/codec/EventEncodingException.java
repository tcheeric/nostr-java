package nostr.event.json.codec;

import lombok.experimental.StandardException;

/**
 * Exception thrown to indicate a problem occurred while encoding a Nostr event to JSON. This
 * exception is typically thrown when the event cannot be serialized due to invalid data or encoding
 * errors.
 */
@StandardException
public class EventEncodingException extends RuntimeException {}
