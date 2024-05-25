//package nostr.event.impl;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NonNull;
//import nostr.event.AbstractEventContent;
//import nostr.event.tag.PriceTag;
//
//@Data
//@EqualsAndHashCode(callSuper = false)
//public class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
//  @JsonProperty
//  private String id;
//
//  @JsonProperty
//  private String title;
//
//  @JsonProperty
//  private String summary;
//
//  @JsonProperty("published_at")
//  @EqualsAndHashCode.Exclude
//  private Long publishedAt;
//
//  @JsonProperty
//  private String location;
//
//  @JsonProperty("price")
//  private PriceTag priceTag;
//
//  public ClassifiedListing(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
//    this.title = title;
//    this.summary = summary;
//    this.priceTag = priceTag;
//  }
//}