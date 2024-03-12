/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.api.factory.TagFactory;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.id.Identity;

import java.net.URL;
import java.util.List;

@Builder(builderMethodName = "mandatoryFieldsBuilder")
public class NIP99 {
  public static final int NIP99 = 99;

  private Identity sender;
  private String content;
  private List<String> price;

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ClassifiedListingEventFactory extends EventFactory<GenericEventNick> {

//    public ClassifiedListingFactory(@NonNull String content) {
//      super(content);
//    }
//
//    public ClassifiedListingFactory(@NonNull Identity sender, @NonNull String content) {
//      super(sender, content);
//    }

//    public ClassifiedListingFactory(@NonNull List<BaseTag> tags, String content) {
//      super(tags, content);
//    }

    public ClassifiedListingEventFactory(@NonNull Identity sender, String content, @NonNull List<BaseTag> price) {
      super(sender, price, content);
    }

    @Override
    public ClassifiedListingEventNick create() {
      List<String> price = List.of(new String[]{"price", "$666", "BTC"});
      GenericEventNick genericEvent = new GenericEventImpl(getSender(), Kind.valueOf(Kinds.CLASSIFIED_LISTING), getTags(), getContent());
      ClassifiedListingEventNick classifiedListingEvent = ClassifiedListingEventNick.builder()
          .genericEvent(genericEvent)
          .content(getContent())
          .price(price)
          .build();
      return classifiedListingEvent;
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class PriceTagFactory extends TagFactory {
    public PriceTagFactory(List<String> params) {
      super("price", NIP99, params);
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class TitleTagFactory extends TagFactory {
    public TitleTagFactory(String title) {
      super("title", NIP99, title);
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ImageTagFactory extends TagFactory {

    public ImageTagFactory(URL url) {
      super("url", NIP99, url.toString());
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class SummaryTagFactory extends TagFactory {

    public SummaryTagFactory(String summary) {
      super("summary", NIP99, summary);
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class PublishedAtTagFactory extends TagFactory {

    public PublishedAtTagFactory(Integer date) {
      super("created_at", NIP99, date.toString());
    }
  }

  public static class Kinds {
    public static final Integer CLASSIFIED_LISTING = 30_402;
  }
}
