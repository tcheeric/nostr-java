package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP99Event;
import nostr.event.json.deserializer.ClassifiedListingEventDeserializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PriceTag;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
@JsonDeserialize(using = ClassifiedListingEventDeserializer.class)
@NoArgsConstructor
public class ClassifiedListingEvent extends NIP99Event {

  public ClassifiedListingEvent(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags, content);
  }

  @Getter
  public enum Status {
    ACTIVE("active"),
    SOLD("sold");

    private final String value;

    Status(@NonNull String value) {
      this.value = value;
    }
  }

  public Instant getPublishedAt() {
    var tag =
        nostr.event.filter.Filterable.requireTagOfTypeWithCode(
            GenericTag.class, "published_at", this);
    return Instant.ofEpochSecond(Long.parseLong(tag.getAttributes().get(0).value().toString()));
  }

  public String getLocation() {
    return nostr.event.filter.Filterable
        .requireTagOfTypeWithCode(GenericTag.class, "location", this)
        .getAttributes()
        .get(0)
        .value()
        .toString();
  }

  public String getTitle() {
    return nostr.event.filter.Filterable
        .requireTagOfTypeWithCode(GenericTag.class, "title", this)
        .getAttributes()
        .get(0)
        .value()
        .toString();
  }

  public String getSummary() {
    return nostr.event.filter.Filterable
        .requireTagOfTypeWithCode(GenericTag.class, "summary", this)
        .getAttributes()
        .get(0)
        .value()
        .toString();
  }

  public String getImage() {
    return nostr.event.filter.Filterable
        .requireTagOfTypeWithCode(GenericTag.class, "image", this)
        .getAttributes()
        .get(0)
        .value()
        .toString();
  }

  public Status getStatus() {
    String status =
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "status", this)
            .getAttributes()
            .get(0)
            .value()
            .toString();
    return Status.valueOf(status);
  }

  public String getPrice() {
    PriceTag priceTag =
        (PriceTag)
            getTags().stream().filter(tag -> tag instanceof PriceTag).findFirst().orElseThrow();

    return priceTag.getNumber().toString()
        + " "
        + priceTag.getCurrency()
        + priceTag.getFrequencyOptional().map(f -> " " + f).orElse("");
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // Validate published_at
    try {
      Long.parseLong(
          nostr.event.filter.Filterable
              .requireTagOfTypeWithCode(GenericTag.class, "published_at", this)
              .getAttributes()
              .get(0)
              .value()
              .toString());
    } catch (java.util.NoSuchElementException e) {
      throw new AssertionError("Missing `published_at` tag for the publication date/time.");
    } catch (NumberFormatException e) {
      throw new AssertionError("Invalid `published_at` tag value: must be a numeric timestamp.");
    }

    // Validate location
    if (nostr.event.filter.Filterable.firstTagOfTypeWithCode(GenericTag.class, "location", this)
        .isEmpty()) {
      throw new AssertionError("Missing `location` tag for the listing location.");
    }

    // Validate title
    if (nostr.event.filter.Filterable.firstTagOfTypeWithCode(GenericTag.class, "title", this)
        .isEmpty()) {
      throw new AssertionError("Missing `title` tag for the listing title.");
    }

    // Validate summary
    if (nostr.event.filter.Filterable.firstTagOfTypeWithCode(GenericTag.class, "summary", this)
        .isEmpty()) {
      throw new AssertionError("Missing `summary` tag for the listing summary.");
    }

    // Validate image
    if (nostr.event.filter.Filterable.firstTagOfTypeWithCode(GenericTag.class, "image", this)
        .isEmpty()) {
      throw new AssertionError("Missing `image` tag for the listing image.");
    }

    // Validate status
    if (nostr.event.filter.Filterable.firstTagOfTypeWithCode(GenericTag.class, "status", this)
        .isEmpty()) {
      throw new AssertionError("Missing `status` tag for the listing status.");
    }
  }

  @Override
  public void validateKind() {
    var n = getKind();
    // Accept only NIP-99 classified listing kinds
    if (n == Kind.CLASSIFIED_LISTING.getValue() || n == Kind.CLASSIFIED_LISTING_INACTIVE.getValue()) {
      return;
    }

    throw new AssertionError(
        String.format(
            "Invalid kind value [%s]. Classified Listing must be either %d or %d",
            n,
            Kind.CLASSIFIED_LISTING.getValue(),
            Kind.CLASSIFIED_LISTING_INACTIVE.getValue()),
        null);
  }
}
