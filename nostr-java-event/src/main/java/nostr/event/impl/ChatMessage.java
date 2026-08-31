package nostr.event.impl;

import lombok.NonNull;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A private chat message, as defined by NIP-17.
 *
 * <p>The recipients plus the sender define a conversation. Adding or removing a participant
 * starts a different conversation with its own history, so the recipient list is not a delivery
 * detail but part of the message's identity.
 *
 * <p>A chat message never travels on its own. It becomes an unsigned rumor, which is sealed and
 * gift wrapped once for each participant before publication.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
public final class ChatMessage {

  private static final String RECIPIENT_TAG = "p";
  private static final String REPLY_TAG = "e";
  private static final String SUBJECT_TAG = "subject";

  private final PublicKey sender;
  private final List<PublicKey> recipients;
  private final String content;
  private final String subject;
  private final String replyTo;
  private final Long createdAt;

  private ChatMessage(Builder builder) {
    this.sender = builder.sender;
    this.recipients = List.copyOf(builder.recipients);
    this.content = builder.content;
    this.subject = builder.subject;
    this.replyTo = builder.replyTo;
    this.createdAt = builder.createdAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Recovers a chat message from a decrypted rumor.
   *
   * @param rumor a kind-14 rumor obtained by unwrapping a gift wrap
   * @return the message it carries
   * @throws IllegalArgumentException if the rumor is not a chat message
   */
  public static ChatMessage from(@NonNull Rumor rumor) {
    if (!Integer.valueOf(Kinds.CHAT_MESSAGE).equals(rumor.getKind())) {
      throw new IllegalArgumentException(
          "Expected a kind-" + Kinds.CHAT_MESSAGE + " chat message but found kind " + rumor.getKind());
    }

    Builder builder =
        builder()
            .from(rumor.getPubKey())
            .content(rumor.getContent())
            .at(rumor.getCreatedAt());

    rumor.getReferencedPublicKeys().forEach(builder::to);
    firstTagValue(rumor, SUBJECT_TAG).ifPresent(builder::subject);
    firstTagValue(rumor, REPLY_TAG).ifPresent(builder::inReplyTo);

    return builder.build();
  }

  /**
   * Renders this message as the unsigned rumor that gets sealed and wrapped.
   *
   * @return a kind-14 rumor carrying this message's content and participants
   */
  public Rumor toRumor() {
    List<BaseTag> tags = new ArrayList<>();
    recipients.forEach(recipient -> tags.add(BaseTag.create(RECIPIENT_TAG, recipient.toString())));
    if (subject != null) {
      tags.add(BaseTag.create(SUBJECT_TAG, subject));
    }
    if (replyTo != null) {
      tags.add(BaseTag.create(REPLY_TAG, replyTo));
    }

    return new Rumor(null, sender, createdAt, Kinds.CHAT_MESSAGE, tags, content);
  }

  /**
   * Returns everyone in this conversation: the recipients and the sender.
   *
   * <p>NIP-17 requires a copy addressed to the sender as well, since a sender who wrapped only
   * for their recipients could never read their own history back.
   *
   * @return each participant once, recipients first
   */
  public List<PublicKey> getParticipants() {
    Set<PublicKey> participants = new LinkedHashSet<>(recipients);
    participants.add(sender);
    return List.copyOf(participants);
  }

  public PublicKey getSender() {
    return sender;
  }

  public List<PublicKey> getRecipients() {
    return Collections.unmodifiableList(recipients);
  }

  public String getContent() {
    return content;
  }

  public Optional<String> getSubject() {
    return Optional.ofNullable(subject);
  }

  public Optional<String> getReplyTo() {
    return Optional.ofNullable(replyTo);
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  private static Optional<String> firstTagValue(Rumor rumor, String code) {
    return rumor.getTags().stream()
        .filter(GenericTag.class::isInstance)
        .map(GenericTag.class::cast)
        .filter(tag -> code.equals(tag.getCode()))
        .filter(tag -> !tag.getParams().isEmpty())
        .map(tag -> tag.getParams().get(0))
        .findFirst();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ChatMessage message)) {
      return false;
    }
    return Objects.equals(sender, message.sender)
        && Objects.equals(recipients, message.recipients)
        && Objects.equals(content, message.content)
        && Objects.equals(subject, message.subject)
        && Objects.equals(replyTo, message.replyTo)
        && Objects.equals(createdAt, message.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sender, recipients, content, subject, replyTo, createdAt);
  }

  @Override
  public String toString() {
    return "ChatMessage(sender=" + sender + ", recipients=" + recipients.size() + ")";
  }

  /** Assembles a chat message, defaulting the timestamp to now. */
  public static final class Builder {

    private final List<PublicKey> recipients = new ArrayList<>();
    private PublicKey sender;
    private String content = "";
    private String subject;
    private String replyTo;
    private Long createdAt;

    private Builder() {}

    /**
     * Names the author. The service supplies this from its identity, so callers rarely set it.
     */
    public Builder from(@NonNull PublicKey sender) {
      this.sender = sender;
      return this;
    }

    /** Adds a recipient. Call more than once for a group conversation. */
    public Builder to(@NonNull PublicKey recipient) {
      if (!recipients.contains(recipient)) {
        recipients.add(recipient);
      }
      return this;
    }

    /** Adds several recipients at once. */
    public Builder to(@NonNull List<PublicKey> newRecipients) {
      newRecipients.forEach(this::to);
      return this;
    }

    /** Sets the message body, which NIP-17 requires to be plain text. */
    public Builder content(@NonNull String content) {
      this.content = content;
      return this;
    }

    /**
     * Titles the conversation. The most recent subject sent to a conversation is its title, so
     * this need not be repeated on every message.
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /** Marks this message as a reply to another chat message. */
    public Builder inReplyTo(String parentEventId) {
      this.replyTo = parentEventId;
      return this;
    }

    /** Overrides the creation time, which otherwise defaults to now. */
    public Builder at(Long createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public ChatMessage build() {
      if (sender == null) {
        throw new IllegalStateException("A chat message needs a sender");
      }
      if (recipients.isEmpty()) {
        throw new IllegalStateException("A chat message needs at least one recipient");
      }
      if (createdAt == null) {
        createdAt = Instant.now().getEpochSecond();
      }
      return new ChatMessage(this);
    }
  }
}
