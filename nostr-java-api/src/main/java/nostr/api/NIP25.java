/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URI;
import java.net.URL;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.Reaction;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EmojiTag;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

public class NIP25 extends EventNostr {

  public NIP25(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a Reaction event
   *
   * @param event the related event to react to
   * @param reaction
   */
  public NIP25 createReactionEvent(
      @NonNull GenericEvent event, @NonNull Reaction reaction, Relay relay) {
    return this.createReactionEvent(event, reaction.getEmoji(), relay);
  }

  /**
   * Create a NIP25 Reaction event to react to a specific event
   *
   * @param event the related event to react to
   * @param content MAY be an emoji
   */
  public NIP25 createReactionEvent(
      @NonNull GenericEvent event, @NonNull String content, Relay relay) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.REACTION, content).create();

    // Addressable event?
    if (event.isAddressable()) {
      genericEvent.addTag(
          NIP01.createAddressTag(event.getKind(), event.getPubKey(), event.getId()));
      genericEvent.addTag(NIP25.createKindTag(event.getKind()));
    } else {
      genericEvent.addTag(
          NIP01.createEventTag(event.getId(), relay != null ? relay.toString() : null, null));
      genericEvent.addTag(NIP01.createPubKeyTag(event.getPubKey()));
    }
    this.updateEvent(genericEvent);
    return this;
  }

  public NIP25 createReactionToWebsiteEvent(@NonNull URL url, @NonNull Reaction reaction) {
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Constants.Kind.REACTION_TO_WEBSITE, reaction.getEmoji())
            .create();
    genericEvent.addTag(NIP12.createReferenceTag(url));
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a NIP25 Reaction event to react to a specific event
   *
   * @param eventTag the e-tag referencing the related event to react to
   * @param emojiTag MUST be an costum emoji (NIP30)
   */
  public NIP25 createReactionEvent(@NonNull BaseTag eventTag, @NonNull BaseTag emojiTag) {

    // 1. Validation
    if (!(emojiTag instanceof EmojiTag)) {
      throw new IllegalArgumentException("The tag is not a custom emoji tag");
    }

    if (!(eventTag instanceof EventTag)) {
      throw new IllegalArgumentException("The tag is not an event tag");
    }

    String shortCode = ((EmojiTag) emojiTag).getShortcode();
    var content = String.format(":%s:", shortCode);

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.REACTION, content).create();
    genericEvent.addTag(emojiTag);
    genericEvent.addTag(eventTag);

    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create the kind tag
   *
   * @param kind the kind
   */
  public static BaseTag createKindTag(@NonNull Integer kind) {
    return new BaseTagFactory(Constants.Tag.KIND_CODE, kind.toString()).create();
  }

  public static BaseTag createCustomEmojiTag(@NonNull String shortcode, @NonNull URL url) {
    return new BaseTagFactory(Constants.Tag.EMOJI_CODE, shortcode, url.toString()).create();
  }

  @SneakyThrows
  public static BaseTag createCustomEmojiTag(@NonNull String shortcode, @NonNull String url) {
    return createCustomEmojiTag(shortcode, URI.create(url).toURL());
  }

  /**
   * @param event
   */
  public void like(@NonNull GenericEvent event, Relay relay) {
    react(event, Reaction.LIKE.getEmoji(), relay);
  }

  public void like(@NonNull GenericEvent event) {
    react(event, Reaction.LIKE.getEmoji(), null);
  }

  /**
   * @param event
   */
  public void dislike(@NonNull GenericEvent event, Relay relay) {
    react(event, Reaction.DISLIKE.getEmoji(), relay);
  }

  public void dislike(@NonNull GenericEvent event) {
    react(event, Reaction.DISLIKE.getEmoji(), null);
  }

  /**
   * @param event
   * @param reaction
   */
  public void react(@NonNull GenericEvent event, @NonNull String reaction, Relay relay) {
    GenericEvent e = createReactionEvent(event, reaction, relay).getEvent();
    this.updateEvent(e);
    this.sign().send();
  }
}
