package nostr.api.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.Getter;
import nostr.api.factory.impl.NIP01Impl;
import nostr.api.factory.impl.NIP99Impl;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.SubjectTag;
import nostr.id.Identity;
import org.apache.commons.lang3.RandomStringUtils;

public class CommonTestObjectsFactory {

  public static Identity createNewIdentity() {
    return Identity.generateRandomIdentity();
  }

  public static <T extends GenericEvent> T createTextNoteEvent(Identity identity, List<BaseTag> tags, String content) {
    TextNoteEvent textNoteEvent = new NIP01Impl.TextNoteEventFactory(identity, tags, content).create();
//    NIP01<NIP01Event> nip01_1 = new NIP01<>(identity);
//    EventNostr sign = nip01_1.createTextNoteEvent(tags, content).sign();
//    return sign;
    return (T) textNoteEvent;
  }

  public static <T extends GenericEvent> T createClassifiedListingEvent(
      Identity identity,
      List<BaseTag> tags,
      String content,
      ClassifiedListing cl) {

    return (T) new NIP99Impl.ClassifiedListingEventFactory(identity, tags, content, cl).create();
  }

  public static GenericEvent createGenericEvent() {
    String concat = generateRandomHex64String();
    return new GenericEvent(concat.substring(0, 64));
  }

  public static <T> SubjectTag createSubjectTag(Class<T> clazz) {
    return new SubjectTag(clazz.getName() + " Subject Tag");
  }

  public static PubKeyTag createPubKeyTag(Identity identity) {
    return new PubKeyTag(identity.getPublicKey());
  }

  public static <T> GeohashTag createGeohashTag(Class<T> clazz) {
    return new GeohashTag(clazz.getName() + " Geohash Tag");
  }

  public static <T> HashtagTag createHashtagTag(Class<T> clazz) {
    return new HashtagTag(clazz.getName() + " Hashtag Tag");
  }

  public static <T> EventTag createEventTag(Class<T> clazz) {
    return new EventTag(createGenericEvent().getId());
  }

  public static PriceTag createPriceTag() {
    PriceComposite pc = new PriceComposite();
    BigDecimal NUMBER = pc.getPrice();
    String CURRENCY = pc.getCurrency();
    String FREQUENCY = pc.getFrequency();
    return new PriceTag(NUMBER, CURRENCY, FREQUENCY);
  }

  public static ClassifiedListing createClassifiedListing(String title, String summary) {
    return new ClassifiedListingComposite(title, summary, createPriceTag()).getClassifiedListing();
  }

  public static <T> String lorumIpsum() {
    return lorumIpsum(CommonTestObjectsFactory.class);
  }

  public static <T> String lorumIpsum(Class<T> clazz) {
    return lorumIpsum(clazz, 64);
  }

  public static <T> String lorumIpsum(Class<T> clazz, int length) {
    return lorumIpsum(clazz.getSimpleName(), length);
  }

  public static <T> String lorumIpsum(String s, int length) {
    boolean useLetters = false;
    boolean useNumbers = true;
    return cullStringLength(
        String.join("-", s, generateRandomAlphaNumericString(length, useLetters, useNumbers))
        , 64);
  }

  public static String lnUrl() {
//  lnurl1dp68gurn8ghj7um5v93kketj9ehx2amn9uh8wetvdskkkmn0wahz7mrww4excup0dajx2mrv92x9xp
//  match lnUrl string length of 84
    return cullStringLength("lnurl" + generateRandomHex64String(), 84);
  }

  private static String cullStringLength(String s, int x) {
    return s.length() > x ? s.substring(0, x) : s;
  }

  private static String generateRandomAlphaNumericString(int length, boolean useLetters, boolean useNumbers) {
    return RandomStringUtils.random(length, useLetters, useNumbers);
  }

  public static String generateRandomHex64String() {
    return UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).replaceAll("[^A-Za-z0-9]", "");
  }

  public static BigDecimal createRandomBigDecimal() {
    Random rand = new Random();
    int max = 100, min = 50;
    int i = rand.nextInt(max - min + 1) + min;
    int j = (rand.nextInt(max - min + 1) + min);
    return new BigDecimal(String.valueOf(i) + '.' + j);
  }

  @Getter
  public static class PriceComposite {
    private final String currency = "BTC";
    private final String frequency = "nanosecond";
    private final BigDecimal price;

    private PriceComposite() {
      price = createRandomBigDecimal();
    }
  }

  @Getter
  public static class ClassifiedListingComposite {
    private final ClassifiedListing classifiedListing;

    private ClassifiedListingComposite(String title, String summary, PriceTag priceTag) {
      this.classifiedListing = ClassifiedListing.builder(
              title,
              summary,
              priceTag)
          .build();
    }
  }
}
