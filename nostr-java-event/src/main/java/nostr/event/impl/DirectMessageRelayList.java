package nostr.event.impl;

import lombok.NonNull;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * The relays on which someone receives private direct messages, as defined by NIP-17.
 *
 * <p>NIP-17 requires a sender to deliver a gift wrap only to the relays its recipient nominated
 * here. Publishing elsewhere does not merely risk non-delivery: it scatters a metadata-bearing
 * event across relays the recipient never chose, while the one they actually read may never see
 * it.
 *
 * <p>A recipient who has published no such list is signalling that they are not ready to
 * receive private messages, and NIP-17 says not to send to them at all.
 *
 * <p>The specification advises keeping this list short, one to three relays, and publishing it
 * widely so senders can find it.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
public final class DirectMessageRelayList {

  private static final String RELAY_TAG = "relay";

  private final PublicKey owner;
  private final List<Relay> relays;
  private final Long createdAt;

  /**
   * Records the relays on which the given key receives private messages.
   *
   * @param owner the key this list belongs to
   * @param relays the relays to nominate, in preference order; duplicates are ignored
   * @param createdAt Unix timestamp in seconds
   */
  public DirectMessageRelayList(
      @NonNull PublicKey owner, @NonNull List<Relay> relays, @NonNull Long createdAt) {
    this.owner = owner;
    this.relays = List.copyOf(new LinkedHashSet<>(relays));
    this.createdAt = createdAt;
  }

  /**
   * Reads a relay list from a kind-10050 event.
   *
   * @param event the event to read
   * @return the relays it nominates
   * @throws IllegalArgumentException if the event is not a DM relay list
   */
  public static DirectMessageRelayList from(@NonNull GenericEvent event) {
    if (!Integer.valueOf(Kinds.DM_RELAY_LIST).equals(event.getKind())) {
      throw new IllegalArgumentException(
          "Expected a kind-"
              + Kinds.DM_RELAY_LIST
              + " direct message relay list but found kind "
              + event.getKind());
    }

    List<Relay> relays = new ArrayList<>();
    for (BaseTag tag : event.getTags()) {
      if (tag instanceof GenericTag generic
          && RELAY_TAG.equals(generic.getCode())
          && !generic.getParams().isEmpty()) {
        relays.add(new Relay(generic.getParams().get(0)));
      }
    }

    return new DirectMessageRelayList(event.getPubKey(), relays, event.getCreatedAt());
  }

  /**
   * Renders this list as the kind-10050 event to publish.
   *
   * <p>The returned event is unsigned; sign it with the owner's identity before publishing.
   *
   * @return the event carrying this list
   */
  public GenericEvent toEvent() {
    List<BaseTag> tags = new ArrayList<>();
    relays.forEach(relay -> tags.add(BaseTag.create(RELAY_TAG, relay.getUri())));

    GenericEvent event = new GenericEvent(owner, Kinds.DM_RELAY_LIST);
    event.setTags(tags);
    event.setContent("");
    event.update(createdAt);
    return event;
  }

  public PublicKey getOwner() {
    return owner;
  }

  public List<Relay> getRelays() {
    return Collections.unmodifiableList(relays);
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  /**
   * Reports whether this list nominates anywhere to deliver a message.
   *
   * <p>An empty list means the owner is not accepting private messages, which is a different
   * situation from a relay being unreachable and should be reported differently.
   *
   * @return true when no relay is nominated
   */
  public boolean isEmpty() {
    return relays.isEmpty();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof DirectMessageRelayList list)) {
      return false;
    }
    return Objects.equals(owner, list.owner)
        && Objects.equals(relays, list.relays)
        && Objects.equals(createdAt, list.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, relays, createdAt);
  }

  @Override
  public String toString() {
    return "DirectMessageRelayList(owner=" + owner + ", relays=" + relays + ")";
  }
}
