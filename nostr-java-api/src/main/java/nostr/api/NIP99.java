/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP99.*;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericEventNick;
import nostr.event.impl.GenericTag;
import nostr.id.Identity;

import java.net.URL;
import java.util.List;

public class NIP99 extends Nostr {

  /**
   * Create a Classified Listingwithout tags
   *
   * @param content a text in Markdown syntax
   * @return
   */
//  public static GenericEvent createClassifiedListing(@NonNull String content) {
//    return new ClassifiedListingFactory(content).create();
//  }
  public static GenericEventNick createClassifiedListingEvent(@NonNull Identity sender, @NonNull String content, @NonNull List<BaseTag> price) {
    return new ClassifiedListingEventFactory(sender, content, price).create();
  }

  /**
   * Create a title tag
   *
   * @param title the article title
   * @return
   */
  public static GenericTag createTitleTag(@NonNull String title) {
    return new TitleTagFactory(title).create();
  }

  /**
   * Create an image tag
   *
   * @param url a URL pointing to an image to be shown along with the title
   * @return
   */
  public static GenericTag createImageTag(@NonNull URL url) {
    return new ImageTagFactory(url).create();
  }

  /**
   * Create a summary tag
   *
   * @param summary the article summary
   * @return
   */
  public static GenericTag createSummaryTag(@NonNull String summary) {
    return new SummaryTagFactory(summary).create();
  }

  /**
   * Create a published_at tag
   *
   * @param date the timestamp in unix seconds (stringified) of the first time the article was published
   * @return
   */
  public static GenericTag createPublishedAtTag(@NonNull Integer date) {
    return new PublishedAtTagFactory(date).create();
  }
}
