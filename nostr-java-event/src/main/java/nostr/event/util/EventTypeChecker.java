package nostr.event.util;

import nostr.base.NipConstants;

/**
 * Utility class for checking Nostr event types based on kind ranges defined in NIP-01.
 *
 * <p>NIP-01 defines three special kind ranges with specific behavior:
 * <ul>
 *   <li><b>Replaceable events (10,000-19,999):</b> Later events with the same kind and author
 *       replace earlier ones. Used for user metadata, contact lists, etc.</li>
 *   <li><b>Ephemeral events (20,000-29,999):</b> Not stored by relays. Used for presence
 *       indicators, typing notifications, etc.</li>
 *   <li><b>Addressable/Parametrized Replaceable events (30,000-39,999):</b> Replaceable events
 *       that can be queried by a 'd' tag parameter. Used for long-form content, product
 *       listings, etc.</li>
 * </ul>
 *
 * <p>Regular events (kind < 10,000 or kind >= 40,000) are immutable and stored indefinitely.
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * int kind = event.getKind();
 *
 * if (EventTypeChecker.isEphemeral(kind)) {
 *     // Don't store, handle immediately
 *     processEphemeralEvent(event);
 * } else if (EventTypeChecker.isReplaceable(kind)) {
 *     // Replace existing event with same kind from same author
 *     replaceEvent(event);
 * } else if (EventTypeChecker.isAddressable(kind)) {
 *     // Replace using kind + author + 'd' tag
 *     String identifier = event.getTagValue("d");
 *     replaceAddressableEvent(event, identifier);
 * } else {
 *     // Store permanently (regular event)
 *     storeEvent(event);
 * }
 *
 * // Get human-readable type name
 * String typeName = EventTypeChecker.getTypeName(kind); // "ephemeral", "replaceable", etc.
 * }</pre>
 *
 * <p><b>Design:</b> This class uses the Utility Pattern with static methods. All methods
 * are stateless and thread-safe.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
 * @see NipConstants
 * @since 0.6.2
 */
public final class EventTypeChecker {

  private EventTypeChecker() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Checks if the event kind is in the replaceable range (10,000-19,999).
   *
   * <p>Replaceable events can be superseded by newer events with the same kind from the same
   * author. Relays should only keep the most recent event.
   *
   * <p>Examples of replaceable event kinds:
   * <ul>
   *   <li>Kind 10000 (0) - Mute list</li>
   *   <li>Kind 10001 - Pin list</li>
   *   <li>Kind 10002 - Relay list metadata</li>
   * </ul>
   *
   * @param kind the event kind to check
   * @return true if kind is in replaceable range, false otherwise
   */
  public static boolean isReplaceable(Integer kind) {
    return kind != null
        && kind >= NipConstants.REPLACEABLE_KIND_MIN
        && kind < NipConstants.REPLACEABLE_KIND_MAX;
  }

  /**
   * Checks if the event kind is in the ephemeral range (20,000-29,999).
   *
   * <p>Ephemeral events are not stored by relays. They are meant for real-time interactions
   * that don't need persistence.
   *
   * <p>Examples of ephemeral event kinds:
   * <ul>
   *   <li>Kind 20000 - Ephemeral event</li>
   *   <li>Kind 22242 - Client authentication (NIP-42)</li>
   * </ul>
   *
   * @param kind the event kind to check
   * @return true if kind is in ephemeral range, false otherwise
   */
  public static boolean isEphemeral(Integer kind) {
    return kind != null
        && kind >= NipConstants.EPHEMERAL_KIND_MIN
        && kind < NipConstants.EPHEMERAL_KIND_MAX;
  }

  /**
   * Checks if the event kind is in the addressable/parametrized replaceable range (30,000-39,999).
   *
   * <p>Addressable events are replaceable events that include a 'd' tag acting as an identifier.
   * They can be queried and replaced using the combination of author pubkey, kind, and 'd' tag
   * value. This allows multiple independent replaceable events of the same kind from one author.
   *
   * <p>Examples of addressable event kinds:
   * <ul>
   *   <li>Kind 30000 - Categorized people list</li>
   *   <li>Kind 30008 - Profile badges</li>
   *   <li>Kind 30009 - Badge definition</li>
   *   <li>Kind 30017 - Create or update a stall (NIP-15)</li>
   *   <li>Kind 30018 - Create or update a product (NIP-15)</li>
   *   <li>Kind 30023 - Long-form content (NIP-23)</li>
   *   <li>Kind 30078 - Application-specific data</li>
   *   <li>Kind 31922-31925 - Calendar events (NIP-52)</li>
   * </ul>
   *
   * @param kind the event kind to check
   * @return true if kind is in addressable range, false otherwise
   */
  public static boolean isAddressable(Integer kind) {
    return kind != null
        && kind >= NipConstants.ADDRESSABLE_KIND_MIN
        && kind < NipConstants.ADDRESSABLE_KIND_MAX;
  }

  /**
   * Checks if the event kind is a regular (non-special) event.
   *
   * <p>Regular events are immutable and stored indefinitely by relays. They don't have special
   * replacement or deletion semantics.
   *
   * <p>Regular event kinds are:
   * <ul>
   *   <li>kind < 10,000 (e.g., kind 0-9,999)</li>
   *   <li>kind >= 40,000</li>
   * </ul>
   *
   * <p>Examples of regular event kinds:
   * <ul>
   *   <li>Kind 0 - Metadata (note: actually replaceable per spec, but kind < 1000)</li>
   *   <li>Kind 1 - Short text note</li>
   *   <li>Kind 3 - Contacts (note: actually replaceable per spec, but kind < 1000)</li>
   *   <li>Kind 4 - Encrypted direct message</li>
   *   <li>Kind 7 - Reaction</li>
   * </ul>
   *
   * @param kind the event kind to check
   * @return true if kind is a regular event, false if it's replaceable, ephemeral, or addressable
   */
  public static boolean isRegular(Integer kind) {
    return kind != null
        && !isReplaceable(kind)
        && !isEphemeral(kind)
        && !isAddressable(kind);
  }

  /**
   * Returns a human-readable type name for the event kind.
   *
   * @param kind the event kind to classify
   * @return type name: "replaceable", "ephemeral", "addressable", or "regular"
   */
  public static String getTypeName(Integer kind) {
    if (isEphemeral(kind)) {
      return "ephemeral";
    } else if (isAddressable(kind)) {
      return "addressable";
    } else if (isReplaceable(kind)) {
      return "replaceable";
    } else {
      return "regular";
    }
  }
}
