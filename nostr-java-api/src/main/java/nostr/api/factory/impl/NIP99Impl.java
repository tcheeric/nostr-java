/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListingEventNick;
import nostr.id.IIdentity;

import java.util.List;

public class NIP99Impl {
  public static final int NIP99 = 99;

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ClassifiedListingEventFactory extends EventFactory<ClassifiedListingEventNick> {
    private String title;
    private String summary;
    private String location;
    private List<String> price;
    private String currency;

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

    public ClassifiedListingEventFactory(@NonNull IIdentity sender, List<BaseTag> tags, String content, String title, String summary, String location, @NonNull List<String> price, String currency) {
      super(sender, tags, content);
      this.title = title;
      this.summary = summary;
      this.location = location;
      this.price = price;
      this.currency = currency;
    }

    @Override
    public ClassifiedListingEventNick create() {
      List<String> price = List.of(new String[]{"price", "$666", "BTC"});
      return new ClassifiedListingEventNick(getSender(), getTags(), getContent(), title, summary, location, price, currency);
    }
  }

  public static class Kinds {
    public static final Integer CLASSIFIED_LISTING = 30_402;
  }
}
