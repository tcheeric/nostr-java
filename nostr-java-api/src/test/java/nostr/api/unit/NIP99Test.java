package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import nostr.api.NIP99;
import nostr.base.Kind;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

/** Unit tests for NIP-99 classified listings (event building and required tags). */
public class NIP99Test {

  @Test
  // Builds a classified listing with title, summary, price and optional fields; verifies tags.
  void createClassifiedListingEvent_withAllFields() throws MalformedURLException {
    Identity sender = Identity.generateRandomIdentity();
    NIP99 nip99 = new NIP99(sender);

    PriceTag price = PriceTag.builder().number(new BigDecimal("19.99")).currency("USD").frequency("day").build();
    ClassifiedListing listing =
        ClassifiedListing.builder("Desk", "Wooden desk", price)
            .publishedAt(1700000000L)
            .location("Seattle, WA")
            .build();

    BaseTag image = nostr.api.NIP23.createImageTag(new URL("https://example.com/image.jpg"), "800x600");
    List<BaseTag> baseTags = List.of(image);

    GenericEvent event =
        nip99.createClassifiedListingEvent(baseTags, "Solid oak.", listing).getEvent();

    // Kind is classified listing
    assertEquals(Kind.CLASSIFIED_LISTING.getValue(), event.getKind());

    // Required NIP-23/NIP-99 tags present
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.TITLE_CODE)));
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.SUMMARY_CODE)));
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.PRICE_CODE)));

    // Optional: published_at, location, image
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.PUBLISHED_AT_CODE)));
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.LOCATION_CODE)));
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.IMAGE_CODE)));

    // Price content integrity
    PriceTag priceTag = (PriceTag) event.getTags().stream()
        .filter(t -> t instanceof PriceTag)
        .findFirst()
        .orElseThrow();
    assertEquals(new BigDecimal("19.99"), priceTag.getNumber());
    assertEquals("USD", priceTag.getCurrency());
    assertEquals("day", priceTag.getFrequency());
  }

  @Test
  // Builds a minimal classified listing with title, summary, and price; verifies required tags only.
  void createClassifiedListingEvent_minimal() {
    Identity sender = Identity.generateRandomIdentity();
    NIP99 nip99 = new NIP99(sender);

    PriceTag price = PriceTag.builder().number(new BigDecimal("100")).currency("EUR").build();
    ClassifiedListing listing = ClassifiedListing.builder("Bike", "Used bike", price).build();

    GenericEvent event =
        nip99.createClassifiedListingEvent(List.of(), "Great condition", listing).getEvent();

    // Kind
    assertEquals(Kind.CLASSIFIED_LISTING.getValue(), event.getKind());
    // Required tags present
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.TITLE_CODE)));
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.SUMMARY_CODE)));
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals(Constants.Tag.PRICE_CODE)));
    // Optional tags absent
    assertTrue(event.getTags().stream().noneMatch(t -> t.getCode().equals(Constants.Tag.PUBLISHED_AT_CODE)));
    assertTrue(event.getTags().stream().noneMatch(t -> t.getCode().equals(Constants.Tag.LOCATION_CODE)));
  }
}

