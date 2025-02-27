package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.api.EventNostr;
import nostr.api.NIP01;
import nostr.api.NIP04;
import nostr.api.NIP15;
import nostr.api.NIP32;
import nostr.api.NIP44;
import nostr.api.NIP52;
import nostr.api.NIP57;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.GeohashTagFilter;
import nostr.event.filter.HashtagTagFilter;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CreateOrUpdateStallEvent;
import nostr.event.impl.CreateOrUpdateStallEvent.Stall;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.EncryptedPayloadEvent;
import nostr.event.impl.GenericTag;
import nostr.event.impl.NostrMarketplaceEvent;
import nostr.event.impl.NostrMarketplaceEvent.Product.Spec;
import nostr.event.impl.TextNoteEvent;
import nostr.event.impl.ZapReceiptEvent;
import nostr.event.impl.ZapRequestEvent;
import nostr.event.message.OkMessage;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author eric
 */
public class ApiEventTest {

  public static final String NOSTR_JAVA_PUBKEY = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";

  private static final Map<String, String> RELAYS = getRelays();

  @Test
  public void testNIP01CreateTextNoteEvent() throws NostrException {
    System.out.println("testNIP01CreateTextNoteEvent");

    PublicKey publicKey = new PublicKey(NOSTR_JAVA_PUBKEY);
    var recipient = NIP01.createPubKeyTag(publicKey);
    List<BaseTag> tags = new ArrayList<>();
    tags.add(recipient);
    Identity identity = Identity.generateRandomIdentity();
    var nip01 = new NIP01<TextNoteEvent>(identity);
    var instance = nip01.createTextNoteEvent(tags, "Hello simplified nostr-java!")
        .getEvent();
    instance.update();

    assertNotNull(instance.getId());
    assertNotNull(instance.getCreatedAt());
    assertNull(instance.getSignature());

    final String bech32 = instance.toBech32();
    assertNotNull(bech32);
    assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
  }

  @Test
  public void testNIP01SendTextNoteEvent() throws IOException {
    System.out.println("testNIP01SendTextNoteEvent");

    Identity identity = Identity.generateRandomIdentity();
    var nip01 = new NIP01<TextNoteEvent>(identity);
    var instance = nip01.createTextNoteEvent("Hello simplified nostr-java!").sign();

    var response = instance.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip01.getEvent().getId(), ((OkMessage) response).getEventId());

    nip01.close();
  }

  @Test
  public void testNIP04SendDirectMessage() throws IOException {
    System.out.println("testNIP04SendDirectMessage");

    PublicKey nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);
    Identity identity = Identity.generateRandomIdentity();
    var nip04 = new NIP04<DirectMessageEvent>(identity, nostr_java);
    var instance = nip04
        .createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...")
        .sign();

    var signature = instance.getEvent().getSignature();
    assertNotNull(signature);
    var response = instance.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip04.getEvent().getId(), ((OkMessage) response).getEventId());
    nip04.close();
  }

  @Test
  public void testNIP44SendDirectMessage() throws IOException {
    System.out.println("testNIP44SendDirectMessage");

    PublicKey nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

    Identity identity = Identity.generateRandomIdentity();
    var nip44 = new NIP44<EncryptedPayloadEvent>(identity, nostr_java);

    var instance = nip44
        .createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...").sign();
    assertNotNull(instance.getEvent().getSignature());
    var response = instance.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip44.getEvent().getId(), ((OkMessage) response).getEventId());
    nip44.close();
  }

  @Test
  public void testNIP01SendTextNoteEventGeoHashTag() throws IOException {
    System.out.println("testNIP01SendTextNoteEventGeoHashTag");

    Identity identity = Identity.generateRandomIdentity();

    String targetString = "geohash_tag-location";
    GeohashTag geohashTag = new GeohashTag(targetString);
    NIP01<NIP01Event> nip01 = new NIP01<>(identity);

    nip01.createTextNoteEvent(List.of(geohashTag), "GeohashTag Test location").signAndSend(Map.of("local", "ws://localhost:5555"));

    Filters filters = new Filters(
        new GeohashTagFilter<>(new GeohashTag(targetString)));

    List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(s -> s.contains(targetString)));

    nip01.close();
  }

  @Test
  public void testNIP01SendTextNoteEventHashtagTag() throws IOException {
    System.out.println("testNIP01SendTextNoteEventHashtagTag");

    Identity identity = Identity.generateRandomIdentity();

    String targetString = "hashtag-tag-value";
    HashtagTag hashtagTag = new HashtagTag(targetString);
    NIP01<NIP01Event> nip01 = new NIP01<>(identity);

    nip01.createTextNoteEvent(List.of(hashtagTag), "Hashtag Tag Test value").signAndSend(Map.of("local", "ws://localhost:5555"));

    Filters filters = new Filters(
        new HashtagTagFilter<>(new HashtagTag(targetString)));

    List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(s -> s.contains(targetString)));

    nip01.close();
  }

  @Test
  public void testNIP01SendTextNoteEventCustomGenericTag() throws IOException {
    System.out.println("testNIP01SendTextNoteEventCustomGenericTag");

    Identity identity = Identity.generateRandomIdentity();

    String targetString = "custom-generic-tag";
    GenericTag genericTag = GenericTag.create("m", 1, targetString);
    NIP01<NIP01Event> nip01 = new NIP01<>(identity);
    nip01.createTextNoteEvent(List.of(genericTag), "Custom Generic Tag Test").signAndSend(Map.of("local", "ws://localhost:5555"));

    Filters filters = new Filters(
        new GenericTagQueryFilter<>(new GenericTagQuery("#m", targetString)));

    List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    String matcher = """
        ["m","custom-generic-tag"]""";

    assertTrue(result.stream().anyMatch(s -> s.contains(matcher)));

    nip01.close();
  }

  @Test
  public void testFiltersListReturnSameSingularEvent() throws IOException {
    System.out.println("testFiltersListReturnSameSingularEvent");

    Identity identity = Identity.generateRandomIdentity();

    String geoHashTagTarget = "geohash_tag-location_SameSingularEvent";
    GeohashTag geohashTag = new GeohashTag(geoHashTagTarget);

    String genericTagTarget = "generic-tag-value_SameSingularEvent";
    GenericTag genericTag = GenericTag.create("m", 1, genericTagTarget);

    NIP01<NIP01Event> nip01 = new NIP01<>(identity);

    nip01.createTextNoteEvent(List.of(geohashTag, genericTag), "Multiple Filters").signAndSend(Map.of("local", "ws://localhost:5555"));

    Filters filters1 = new Filters(
        new GeohashTagFilter<>(new GeohashTag(geoHashTagTarget)));
    Filters filters2 = new Filters(
        new GenericTagQueryFilter<>(new GenericTagQuery("#m", genericTagTarget)));

    List<String> result = nip01.sendRequest(List.of(filters1, filters2), UUID.randomUUID().toString());

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(s -> s.contains(geoHashTagTarget)));

    nip01.close();
  }

  @Test
  public void testFiltersListReturnTwoDifferentEvents() throws IOException {
    System.out.println("testFiltersListReturnTwoDifferentEvents");

//    first event
    Identity identity1 = Identity.generateRandomIdentity();
    String geoHashTagTarget1 = "geohash_tag-location-1";
    GeohashTag geohashTag1 = new GeohashTag(geoHashTagTarget1);
    String genericTagTarget1 = "generic-tag-value-1";
    GenericTag genericTag1 = GenericTag.create("m", 1, genericTagTarget1);
    NIP01<NIP01Event> nip01_1 = new NIP01<>(identity1);
    nip01_1.createTextNoteEvent(List.of(geohashTag1, genericTag1), "Multiple Filters 1").signAndSend(Map.of("local", "ws://localhost:5555"));

//    second event
    Identity identity2 = Identity.generateRandomIdentity();
    String geoHashTagTarget2 = "geohash_tag-location-2";
    GeohashTag geohashTag2 = new GeohashTag(geoHashTagTarget2);
    String genericTagTarget2 = "generic-tag-value-2";
    GenericTag genericTag2 = GenericTag.create("m", 1, genericTagTarget2);
    NIP01<NIP01Event> nip01_2 = new NIP01<>(identity2);
    nip01_2.createTextNoteEvent(List.of(geohashTag2, genericTag2), "Multiple Filters 2").signAndSend(Map.of("local", "ws://localhost:5555"));

    Filters filters1 = new Filters(
        new GeohashTagFilter<>(new GeohashTag(geoHashTagTarget1)));  // 1st filter should find match in 1st event

    Filters filters2 = new Filters(
        new GenericTagQueryFilter<>(new GenericTagQuery("#m", genericTagTarget2)));  // 2nd filter should find match in 2nd event

    List<String> result = nip01_1.sendRequest(List.of(filters1, filters2), UUID.randomUUID().toString());

    assertFalse(result.isEmpty());
    assertEquals(3, result.size());
    assertTrue(result.stream().anyMatch(s -> s.contains(geoHashTagTarget1)));
    assertTrue(result.stream().anyMatch(s -> s.contains(genericTagTarget2)));

    nip01_1.close();
    nip01_2.close();
  }

  @Test
  public void testMultipleFiltersDifferentTypesReturnSameEvent() throws IOException {
    System.out.println("testMultipleFilters");

    Identity identity = Identity.generateRandomIdentity();

    String geoHashTagTarget = "geohash_tag-location-DifferentTypesReturnSameEvent";
    GeohashTag geohashTag = new GeohashTag(geoHashTagTarget);

    String genericTagTarget = "generic-tag-value-DifferentTypesReturnSameEvent";
    GenericTag genericTag = GenericTag.create("m", 1, genericTagTarget);

    NIP01<NIP01Event> nip01 = new NIP01<>(identity);

    nip01.createTextNoteEvent(List.of(geohashTag, genericTag), "Multiple Filters").signAndSend(Map.of("local", "ws://localhost:5555"));

    Filters filters = new Filters(
        new GeohashTagFilter<>(new GeohashTag(geoHashTagTarget)),
        new GenericTagQueryFilter<>(new GenericTagQuery("#m", genericTagTarget)));

    List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(s -> s.contains(geoHashTagTarget)));

    nip01.close();
  }

  @Test
  public void testNIP04EncryptDecrypt() {
    System.out.println("testNIP04EncryptDecrypt");

    var nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);
    Identity identity = Identity.generateRandomIdentity();
    var nip04 = new NIP04<DirectMessageEvent>(identity, nostr_java);
    var instance = nip04
        .createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...")
        .sign();

    var message = NIP04.decrypt(identity, instance.getEvent());

    assertEquals("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...", message);
  }

  @Test
  public void testNIP44EncryptDecrypt() {
    System.out.println("testNIP44EncryptDecrypt");

    var nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

    Identity identity = Identity.generateRandomIdentity();
    var nip44 = new NIP44<EncryptedPayloadEvent>(identity, nostr_java);

    var instance = nip44
        .createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...").sign();
    var message = NIP44.decrypt(identity, instance.getEvent());

    assertEquals("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...", message);
  }

  @Test
  public void testNIP15CreateStallEvent() throws JsonProcessingException {
    System.out.println("testNIP15CreateStallEvent");

    Stall stall = createStall();
    var nip15 = new NIP15<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    // Create and send the nostr event
    var instance = nip15.createCreateOrUpdateStallEvent(stall).sign();
    var signature = instance.getEvent().getSignature();
    assertNotNull(signature);

    // Fetch the content and compare with the above original
    var content = instance.getEvent().getContent();
    ObjectMapper mapper = new ObjectMapper();
    var expected = mapper.readValue(content, Stall.class);

    assertEquals(expected, stall);
  }

  @Test
  public void testNIP15UpdateStallEvent() throws IOException {
    System.out.println("testNIP15UpdateStallEvent");

    var stall = createStall();
    var nip15 = new NIP15<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    // Create and send the nostr event
    var instance = nip15.createCreateOrUpdateStallEvent(stall).sign();
    var signature = instance.getEvent().getSignature();
    assertNotNull(signature);

    var response = instance.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

    // Update the shipping
    var shipping = stall.getShipping();
    shipping.setCost(20.00f);

    EventNostr event = nip15.createCreateOrUpdateStallEvent(stall).sign();
    response = event.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

    nip15.close();
  }

  @Test
  public void testNIP15CreateProductEvent() throws IOException {

    System.out.println("testNIP15CreateProductEvent");

    // Create the stall object
    var stall = createStall();
    var nip15 = new NIP15<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    // Create the product
    var product = createProduct(stall);

    List<String> categories = new ArrayList<>();
    categories.add("bijoux");
    categories.add("Hommes");

    EventNostr event = nip15.createCreateOrUpdateProductEvent(product, categories).sign();
    var response = event.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

    nip15.close();
  }

  @Test
  public void testNIP15UpdateProductEvent() throws IOException {

    System.out.println("testNIP15UpdateProductEvent");

    // Create the stall object
    var stall = createStall();
    var nip15 = new NIP15<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    // Create the product
    var product = createProduct(stall);

    List<String> categories = new ArrayList<>();
    categories.add("bijoux");
    categories.add("Hommes");

    EventNostr event1 = nip15.createCreateOrUpdateProductEvent(product, categories).sign();
    var response = event1.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

    product.setDescription("Un nouveau bijou en or");
    categories.add("bagues");

    EventNostr event2 = nip15.createCreateOrUpdateProductEvent(product, categories).sign();
    response = event2.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

    nip15.close();
  }

  @Test
  public void testNIP32CreateNameSpace() {

    System.out.println("testNIP32CreateNameSpace");

    var langNS = NIP32.createNameSpaceTag("Languages");

    assertEquals("L", langNS.getCode());
    assertEquals(1, langNS.getAttributes().size());
    assertEquals("Languages", langNS.getAttributes().iterator().next().getValue());
  }

  @Test
  public void testNIP32CreateLabel1() {

    System.out.println("testNIP32CreateLabel1");

    var label = NIP32.createLabelTag("Languages", "english");

    assertEquals("l", label.getCode());
    assertEquals(2, label.getAttributes().size());
    assertTrue(label.getAttributes().contains(new ElementAttribute("param0", "english", 32)));
    assertTrue(label.getAttributes().contains(new ElementAttribute("param1", "Languages", 32)));
  }

  @Test
  public void testNIP32CreateLabel2() {

    System.out.println("testNIP32CreateLabel2");

    var metadata = new HashMap<String, Object>();
    metadata.put("article", "the");
    var label = NIP32.createLabelTag("Languages", "english", metadata);

    assertEquals("l", label.getCode());
    assertEquals(3, label.getAttributes().size());
    assertTrue(label.getAttributes().contains(new ElementAttribute("param0", "english", 32)));
    assertTrue(label.getAttributes().contains(new ElementAttribute("param1", "Languages", 32)));
    assertTrue(label.getAttributes().contains(new ElementAttribute("param2", "{\\\"article\\\":\\\"the\\\"}", 32)),
        "{\\\"article\\\":\\\"the\\\"}");
  }

  @Test
  public void testNIP52CalendarTimeBasedEventEvent() throws IOException {
    System.out.println("testNIP52CalendarTimeBasedEventEvent");

    CalendarContent calendarContent = CalendarContent.builder(
        new IdentifierTag("UUID-CalendarTimeBasedEventTest"),
        "Calendar Time-Based Event title",
        1716513986268L).build();

    calendarContent.setStartTzid("1687765220");
    calendarContent.setEndTzid("1687765230");

    calendarContent.setLabels(List.of("english", "mycenaean greek"));

    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985"),
        "ws://localhost:5555",
        "ISSUER"));
    tags.add(new PubKeyTag(new PublicKey("494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347"),
        "",
        "COUNTERPARTY"));

    var nip52 = new NIP52<>(Identity.create(PrivateKey.generateRandomPrivKey()));
    EventNostr event = nip52.createCalendarTimeBasedEvent(tags, "content", calendarContent).sign();
    var response = event.setRelays(RELAYS).send();
    assertTrue(response instanceof OkMessage);
    assertEquals(nip52.getEvent().getId(), ((OkMessage) response).getEventId());

    nip52.close();
  }

  @Test
  void testNIP57CreateZapRequestEvent() throws NostrException {
    System.out.println("testNIP57CreateZapRequestEvent");

    Identity sender = Identity.generateRandomIdentity();
    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    PublicKey recipient = Identity.generateRandomIdentity().getPublicKey();
    var nip57 = new NIP57<ZapRequestEvent>(sender);
    final String ZAP_REQUEST_CONTENT = "zap request content";
    final Long AMOUNT = 1232456L;
    final String LNURL = "lnUrl";
    final String RELAYS_TAG = "ws://localhost:5555";
    ZapRequestEvent instance = nip57
        .createZapRequestEvent(recipient, baseTags, ZAP_REQUEST_CONTENT, AMOUNT, LNURL, RELAYS_TAG).getEvent();
    instance.update();

    assertNotNull(instance.getId());
    assertNotNull(instance.getCreatedAt());
    assertNotNull(instance.getContent());
    assertNull(instance.getSignature());

    assertNotNull(instance.getZapRequest());
    assertNotNull(instance.getZapRequest().getRelaysTag());
    assertNotNull(instance.getZapRequest().getAmount());
    assertNotNull(instance.getZapRequest().getLnUrl());

    assertEquals(ZAP_REQUEST_CONTENT, instance.getContent());
    assertTrue(instance.getZapRequest().getRelaysTag().getRelays().stream()
        .anyMatch(relay -> relay.getUri().equals(RELAYS_TAG)));
    assertEquals(AMOUNT, instance.getZapRequest().getAmount());
    assertEquals(LNURL, instance.getZapRequest().getLnUrl());

    final String bech32 = instance.toBech32();
    assertNotNull(bech32);
    assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
  }

  @Test
  void testNIP57CreateZapReceiptEvent() throws NostrException {
    System.out.println("testNIP57CreateZapReceiptEvent");

    Identity sender = Identity.generateRandomIdentity();
    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    String zapRequestPubKeyTag = Identity.generateRandomIdentity().getPublicKey().toString();
    String zapRequestEventTag = Identity.generateRandomIdentity().getPublicKey().toString();
    String zapRequestAddressTag = Identity.generateRandomIdentity().getPublicKey().toString();
    final String ZAP_RECEIPT_IDENTIFIER = "ipsum";
    final String ZAP_RECEIPT_RELAY_URI = "ws://localhost:5555";
    final String BOLT_11 = "bolt11";
    final String DESCRIPTION_SHA256 = "descriptionSha256";
    final String PRE_IMAGE = "preimage";
    var nip57 = new NIP57<ZapReceiptEvent>(sender);

    ZapReceiptEvent instance = nip57.createZapReceiptEvent(zapRequestPubKeyTag, baseTags, zapRequestEventTag,
            zapRequestAddressTag, ZAP_RECEIPT_IDENTIFIER, ZAP_RECEIPT_RELAY_URI, BOLT_11, DESCRIPTION_SHA256, PRE_IMAGE)
        .getEvent();
    instance.update();

    assertNotNull(instance.getId());
    assertNotNull(instance.getCreatedAt());
    assertNull(instance.getSignature());

    assertNotNull(instance.getZapReceipt());
    assertNotNull(instance.getZapReceipt().getBolt11());
    assertNotNull(instance.getZapReceipt().getDescriptionSha256());
    assertNotNull(instance.getZapReceipt().getPreimage());

    assertEquals(BOLT_11, instance.getZapReceipt().getBolt11());
    assertEquals(DESCRIPTION_SHA256, instance.getZapReceipt().getDescriptionSha256());
    assertEquals(PRE_IMAGE, instance.getZapReceipt().getPreimage());

    final String bech32 = instance.toBech32();
    assertNotNull(bech32);
    assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
  }

  public static Stall createStall() {

    // Create the county list
    List<String> countries = new ArrayList<>();
    countries.add("France");
    countries.add("Canada");
    countries.add("Cameroun");

    // Create the shipping object
    var shipping = new CreateOrUpdateStallEvent.Stall.Shipping();
    shipping.setCost(12.00f);
    shipping.setCountries(countries);
    shipping.setName("French Countries");

    // Create the stall object
    var stall = new CreateOrUpdateStallEvent.Stall();
    stall.setCurrency("USD");
    stall.setDescription("This is a test stall");
    stall.setName("Maximus Primus");
    stall.setShipping(shipping);

    return stall;
  }

  public static NostrMarketplaceEvent.Product createProduct(Stall stall) {

    // Create the product
    var product = new NostrMarketplaceEvent.Product();
    product.setCurrency("USD");
    product.setDescription("Un bijou en or");
    product.setImages(new ArrayList<>());
    product.setName("Bague");
    product.setPrice(450.00f);
    product.setQuantity(4);
    List<Spec> specs = new ArrayList<>();
    specs.add(new Spec("couleur", "or"));
    specs.add(new Spec("poids", "150g"));
    product.setSpecs(specs);
    product.setStall(stall);

    return product;
  }

  public static Map<String, String> getRelays() {
    Map<String, String> relays = new HashMap<>();
    Properties properties = new Properties();
    try {
      InputStream is = ApiEventTest.class.getClassLoader().getResourceAsStream("relays.properties");
      if (is != null) {
        properties.load(is);
        for (String key : properties.stringPropertyNames()) {
          relays.put(key, properties.getProperty(key));
        }
      } else {
        throw new RuntimeException("Unable to find 'relays.properties' in the classpath");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return relays;
  }
}
