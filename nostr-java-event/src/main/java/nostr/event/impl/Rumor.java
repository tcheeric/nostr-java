package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.json.deserializer.PublicKeyDeserializer;
import nostr.event.serializer.EventSerializer;
import nostr.event.tag.GenericTag;
import nostr.util.NostrException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An unsigned Nostr event, as defined by NIP-59.
 *
 * <p>A rumor carries content and identifies its author, but carries no signature. That absence is
 * the point: a leaked rumor cannot be authenticated, which gives its author deniability, and
 * relays reject it. Rumors travel only inside a seal, which is signed and encrypted.
 *
 * <p>This type deliberately does not extend {@link GenericEvent} and does not implement {@code
 * ISignable}. A rumor that acquired a signature would defeat NIP-59, so the type system forbids
 * it rather than relying on callers to remember.
 *
 * <p>Instances are immutable. The {@code id} is derived from the remaining fields using the
 * NIP-01 canonical serialization, so it is computed rather than supplied.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Rumor {

  @JsonProperty("id")
  private final String id;

  @JsonProperty("pubkey")
  @JsonDeserialize(using = PublicKeyDeserializer.class)
  private final PublicKey pubKey;

  @JsonProperty("created_at")
  private final Long createdAt;

  @JsonProperty("kind")
  private final Integer kind;

  @JsonProperty("tags")
  private final List<BaseTag> tags;

  @JsonProperty("content")
  private final String content;

  /**
   * Reconstructs a rumor from its parts, typically after decrypting a seal.
   *
   * <p>The supplied id is retained as-is so that a received rumor can be checked against a
   * recomputed id; use {@link #hasValidId()} to perform that check.
   *
   * @param id the event id carried by the rumor, or {@code null} to derive one
   * @param pubKey the author's public key
   * @param createdAt Unix timestamp in seconds
   * @param kind the event kind, typically {@link nostr.base.Kinds#CHAT_MESSAGE}
   * @param tags the event tags; may be empty but not null
   * @param content the message content
   */
  @JsonCreator
  public Rumor(
      @JsonProperty("id") String id,
      @JsonProperty("pubkey") @NonNull PublicKey pubKey,
      @JsonProperty("created_at") @NonNull Long createdAt,
      @JsonProperty("kind") @NonNull Integer kind,
      @JsonProperty("tags") @NonNull List<BaseTag> tags,
      @JsonProperty("content") @NonNull String content) {
    this.pubKey = pubKey;
    this.createdAt = createdAt;
    this.kind = kind;
    this.tags = List.copyOf(tags);
    this.content = content;
    this.id = id != null ? id : computeId(pubKey, createdAt, kind, this.tags, content);
  }

  /**
   * Creates a rumor stamped with the current time and a derived id.
   *
   * @param pubKey the author's public key
   * @param kind the event kind, typically {@link nostr.base.Kinds#CHAT_MESSAGE}
   * @param tags the event tags; may be empty but not null
   * @param content the message content
   * @return a new rumor whose id is derived from its contents
   */
  public static Rumor create(
      @NonNull PublicKey pubKey,
      @NonNull Integer kind,
      @NonNull List<BaseTag> tags,
      @NonNull String content) {
    return new Rumor(null, pubKey, Instant.now().getEpochSecond(), kind, tags, content);
  }

  public String getId() {
    return id;
  }

  public PublicKey getPubKey() {
    return pubKey;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public Integer getKind() {
    return kind;
  }

  public List<BaseTag> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public String getContent() {
    return content;
  }

  /**
   * Reports whether this rumor's id matches the id derived from its contents.
   *
   * <p>A received rumor arrives with an id chosen by whoever sealed it. Recomputing that id
   * detects a rumor whose contents were altered after its id was set.
   *
   * @return true when the carried id matches the derived id
   */
  @JsonIgnore
  public boolean hasValidId() {
    return computeId(pubKey, createdAt, kind, tags, content).equals(id);
  }

  /**
   * Returns the public keys named by this rumor's {@code p} tags, in order.
   *
   * <p>For a NIP-17 chat message these are the recipients of the conversation.
   *
   * @return the referenced public keys; empty when the rumor has no {@code p} tags
   */
  @JsonIgnore
  public List<PublicKey> getReferencedPublicKeys() {
    List<PublicKey> referenced = new ArrayList<>();
    for (BaseTag tag : tags) {
      if (tag instanceof GenericTag generic
          && "p".equals(generic.getCode())
          && !generic.getParams().isEmpty()) {
        referenced.add(new PublicKey(generic.getParams().get(0)));
      }
    }
    return referenced;
  }

  private static String computeId(
      PublicKey pubKey, Long createdAt, Integer kind, List<BaseTag> tags, String content) {
    try {
      return EventSerializer.computeEventId(
          EventSerializer.serializeToBytes(pubKey, createdAt, kind, tags, content));
    } catch (NostrException ex) {
      throw new IllegalStateException("Failed to compute rumor id", ex);
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Rumor rumor)) {
      return false;
    }
    return Objects.equals(id, rumor.id)
        && Objects.equals(pubKey, rumor.pubKey)
        && Objects.equals(createdAt, rumor.createdAt)
        && Objects.equals(kind, rumor.kind)
        && Objects.equals(tags, rumor.tags)
        && Objects.equals(content, rumor.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pubKey, createdAt, kind, tags, content);
  }

  @Override
  public String toString() {
    return "Rumor(id=" + id + ", pubKey=" + pubKey + ", kind=" + kind + ")";
  }
}
