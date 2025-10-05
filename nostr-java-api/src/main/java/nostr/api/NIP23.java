/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * NIP-23 helpers (Long-form content). Build long-form notes and related tags.
 * Spec: https://github.com/nostr-protocol/nips/blob/master/23.md
 */
public class NIP23 extends EventNostr {

  public NIP23(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a Long-form Content event with tags
   *
   * @param content a text in Markdown syntax
   */
  public NIP23 creatLongFormTextNoteEvent(@NonNull String content) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.LONG_FORM_TEXT_NOTE, content).create();
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a Long-form Draft event (kind 30023) that is not intended for indexing.
   *
   * @param content a text in Markdown syntax for the draft
   * @return this instance for chaining
   */
  NIP23 createLongFormDraftEvent(@NonNull String content) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.LONG_FORM_DRAFT, content).create();
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Add a title tag to the long-form content event.
   *
   * @param title the article title
   * @return this instance for chaining
   */
  public NIP23 addTitleTag(@NonNull String title) {
    getEvent().addTag(createTitleTag(title));
    return this;
  }

  /**
   * Add an image tag to the long-form content event.
   *
   * @param url URL of the image to be shown with the title
   * @return this instance for chaining
   */
  public NIP23 addImageTag(@NonNull URL url) {
    getEvent().addTag(createImageTag(url));
    return this;
  }

  /**
   * Add a summary tag to the long-form content event.
   *
   * @param summary the article summary
   * @return this instance for chaining
   */
  public NIP23 addSummaryTag(@NonNull String summary) {
    getEvent().addTag(createSummaryTag(summary));
    return this;
  }

  /**
   * Add a published_at tag to the long-form content event.
   *
   * @param date timestamp in unix seconds (stringified) when the article was first published
   * @return this instance for chaining
   */
  public NIP23 addPublishedAtTag(@NonNull Long date) {
    getEvent().addTag(createPublishedAtTag(date));
    return this;
  }

  /**
   * Create a title tag
   *
   * @param title the article title
   */
  public static BaseTag createTitleTag(@NonNull String title) {
    return new BaseTagFactory("title", title).create();
  }

  /**
   * Create an image tag
   *
   * @param url a URL pointing to an image to be shown along with the title
   */
  public static BaseTag createImageTag(@NonNull URL url) {
    return new BaseTagFactory(Constants.Tag.IMAGE_CODE, url.toString()).create();
  }

  /**
   * Create an image tag
   *
   * @param url a URL pointing to an image to be shown along with the title
   * @param size the size of the image
   */
  public static BaseTag createImageTag(@NonNull URL url, String size) {
    return new BaseTagFactory(Constants.Tag.IMAGE_CODE, url.toString(), size).create();
  }

  /**
   * Create a summary tag
   *
   * @param summary the article summary
   */
  public static BaseTag createSummaryTag(@NonNull String summary) {
    return new BaseTagFactory(Constants.Tag.SUMMARY_CODE, summary).create();
  }

  /**
   * Create a published_at tag
   *
   * @param date the timestamp in unix seconds (stringified) of the first time the article was
   *     published
   */
  public static BaseTag createPublishedAtTag(@NonNull Long date) {
    return new BaseTagFactory(Constants.Tag.PUBLISHED_AT_CODE, date.toString()).create();
  }
}
