package nostr.event;

import lombok.NoArgsConstructor;
import nostr.base.IEvent;

/**
 * Base class for all Nostr event implementations.
 *
 * <p>This abstract class provides a common foundation for all event types in the Nostr protocol.
 * It implements the {@link IEvent} interface which defines the core contract for events,
 * including event ID retrieval and Bech32 encoding support (NIP-19).
 *
 * <p><b>Hierarchy:</b>
 * <pre>
 * BaseEvent (abstract)
 *   ├─ GenericEvent (NIP-01 implementation)
 *   │   ├─ CustomEmojiEvent (NIP-30)
 *   │   ├─ EncryptedDirectMessageEvent (NIP-04)
 *   │   ├─ GenericMetadataEvent (NIP-01)
 *   │   ├─ CalendarEvent (NIP-52)
 *   │   └─ ... (other NIP-specific events)
 *   └─ Other custom event implementations
 * </pre>
 *
 * <p><b>Design:</b> This class follows the Template Method pattern, providing the base
 * structure while allowing subclasses to implement specific event behavior. Most event
 * implementations extend {@link nostr.event.impl.GenericEvent} which provides the full
 * NIP-01 event structure.
 *
 * <p><b>Usage:</b> Typically, you don't extend this class directly. Instead:
 * <ul>
 *   <li>Use {@link nostr.event.impl.GenericEvent} for basic NIP-01 events</li>
 *   <li>Extend {@link nostr.event.impl.GenericEvent} for NIP-specific events</li>
 *   <li>Only extend {@code BaseEvent} directly for custom, non-standard event types</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * // Most common: Use GenericEvent directly
 * GenericEvent event = GenericEvent.builder()
 *     .kind(Kind.TEXT_NOTE)
 *     .content("Hello Nostr!")
 *     .build();
 *
 * // Or use NIP-specific implementations that extend GenericEvent
 * CalendarEvent calendarEvent = CalendarEvent.builder()
 *     .name("Nostr Conference 2025")
 *     .start(startTime)
 *     .build();
 * }</pre>
 *
 * @see nostr.event.impl.GenericEvent
 * @see IEvent
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
 * @since 0.1.0
 */
@NoArgsConstructor
public abstract class BaseEvent implements IEvent {}
