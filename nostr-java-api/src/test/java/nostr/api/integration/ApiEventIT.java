package nostr.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nostr.api.EventNostr;
import nostr.api.NIP01;
import nostr.api.NIP04;
import nostr.api.NIP15;
import nostr.api.NIP52;
import nostr.api.NIP57;
import nostr.base.GenericTagQuery;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.RelayConfig;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.entities.Product;
import nostr.event.entities.Stall;
import nostr.event.entities.ZapReceipt;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.GeohashTagFilter;
import nostr.event.filter.HashtagTagFilter;
import nostr.event.filter.UrlTagFilter;
import nostr.event.filter.VoteTagFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.UrlTag;
import nostr.event.tag.VoteTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(RelayConfig.class)
@Slf4j
public class ApiEventIT extends BaseRelayIntegrationTest {
    @Autowired
    private Map<String, String> relays;

    @Test
    public void testNIP01CreateTextNoteEvent() throws Exception {
        System.out.println("testNIP01CreateTextNoteEvent");

        var nip01 = new NIP01(Identity.generateRandomIdentity());
        var instance = nip01.createTextNoteEvent(
                        List.of(NIP01.createPubKeyTag(Identity.generateRandomIdentity().getPublicKey())),
                        "Hello simplified nostr-java!")
                .getEvent();
        instance.update();

        assertNotNull(instance.getId());
        assertNotNull(instance.getCreatedAt());
        assertNull(instance.getSignature());

        final String bech32 = instance.toBech32();
        assertNotNull(bech32);
        assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);

        await().atMost(Duration.ofSeconds(3));
    }

    @Test
    public void testNIP01SendTextNoteEvent() throws IOException {
        System.out.println("testNIP01SendTextNoteEvent");

        var nip01 = new NIP01(Identity.generateRandomIdentity());
        var instance = nip01.createTextNoteEvent("Hello simplified nostr-java!").sign();

        var response = instance.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip01.getEvent().getId(), ((OkMessage) response).getEventId());

        nip01.close();
    }

    @Test
    public void testNIP04SendDirectMessage() throws IOException {
        System.out.println("testNIP04SendDirectMessage");

        var nip04 = new NIP04(
                Identity.generateRandomIdentity(),
                Identity.generateRandomIdentity().getPublicKey());

        var instance = nip04
                .createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...")
                .sign();

        var signature = instance.getEvent().getSignature();
        assertNotNull(signature);
        var response = instance.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip04.getEvent().getId(), ((OkMessage) response).getEventId());

        nip04.close();
    }

    @Test
    public void testNIP01SendTextNoteEventGeoHashTag() throws IOException {
        System.out.println("testNIP01SendTextNoteEventGeoHashTag");

        String targetString = "geohash_tag-location-testNIP01SendTextNoteEventGeoHashTag";
        GeohashTag geohashTag = new GeohashTag(targetString);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent(List.of(geohashTag), "GeohashTag Test location testNIP01SendTextNoteEventGeoHashTag").signAndSend(relays);

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

        String targetString = "hashtag-tag-value-testNIP01SendTextNoteEventHashtagTag";
        HashtagTag hashtagTag = new HashtagTag(targetString);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent(List.of(hashtagTag), "Hashtag Tag Test value testNIP01SendTextNoteEventHashtagTag").signAndSend(relays);

        Filters filters = new Filters(
                new HashtagTagFilter<>(new HashtagTag(targetString)));

        List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        //assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.contains(targetString)));

        nip01.close();
    }

    @Test
    public void testNIP01SendTextNoteEventCustomGenericTag() throws IOException {
        System.out.println("testNIP01SendTextNoteEventCustomGenericTag");

        String targetString = "custom-generic-tag-testNIP01SendTextNoteEventCustomGenericTag";
        BaseTag genericTag = BaseTag.create("m", targetString);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent(List.of(genericTag), "Custom Generic Tag Test testNIP01SendTextNoteEventCustomGenericTag").signAndSend(relays);

        Filters filters = new Filters(
                new GenericTagQueryFilter<>(new GenericTagQuery("#m", targetString)));

        List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        String matcher = """
                ["m","custom-generic-tag-testNIP01SendTextNoteEventCustomGenericTag"]""";

        assertTrue(result.stream().anyMatch(s -> s.contains(matcher)));

        nip01.close();
    }

    @Test
    public void testNIP01SendTextNoteEventRecipientGenericTag() throws IOException {
        System.out.println("testNIP01SendTextNoteEventRecipientGenericTag");

        Identity recipientIdentity = Identity.generateRandomIdentity();

        PubKeyTag recipientTag = (PubKeyTag) NIP01.createPubKeyTag(recipientIdentity.getPublicKey());
        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent("testNIP01SendTextNoteEventRecipientGenericTag", List.of(recipientTag)).signAndSend(relays);

        Filters filters = new Filters(
                new GenericTagQueryFilter<>(new GenericTagQuery("#p", recipientTag.getPublicKey().toString())));

        List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        String matcher = """
                ["p","%s"]""".formatted(recipientTag.getPublicKey().toString());

        assertTrue(result.stream().anyMatch(s -> s.contains(matcher)));

        nip01.close();
    }

    @Test
    public void testNIP01SendTextNoteEventUrlTag() throws IOException {
        System.out.println("testNIP01SendTextNoteEventUrlTag");

        String targetString = getRelayUri();
        BaseTag genericTag = BaseTag.create("u", targetString);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent(List.of(genericTag), "testNIP01SendTextNoteEventUrlTag").signAndSend(relays);

        Filters filters = new Filters(
                new GenericTagQueryFilter<>(new GenericTagQuery("#u", targetString)));

        List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        assertEquals(2, result.size());

        String matcher = """
                ["u","%s"]""".formatted(targetString);

        assertTrue(result.stream().anyMatch(s -> s.contains(matcher)));

        nip01.close();
    }

    @Test
    public void testFilterUrlTag() throws IOException {
        System.out.println("testFilterUrlTag");

        String targetString = getRelayUri().replace("ws://", "https://");
        //UrlTag urlTag = new UrlTag(targetString);
        BaseTag urlTag = BaseTag.create("u", targetString);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent(List.of(urlTag), "testFilterUrlTag").signAndSend(relays);

        Filters filters = new Filters(
                new UrlTagFilter<>(new UrlTag(targetString)));

        List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        assertEquals(2, result.size(), result.toString());

        String matcher = """
                ["u","%s"]""".formatted(targetString);

        assertTrue(result.stream().anyMatch(s -> s.contains(matcher)));

        List<BaseMessage> messages = result.stream()
                .map(json -> {
                    try {
                        return new BaseMessageDecoder<>().decode(json);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        assertEquals(2, messages.size());

        assertInstanceOf(EventMessage.class, messages.get(0));
        assertInstanceOf(EoseMessage.class, messages.get(1));

        GenericEvent event = (GenericEvent) ((EventMessage) messages.get(0)).getEvent();

        Optional<UrlTag> optionalUrlTag = event.getTags().stream()
                .filter(t -> t instanceof UrlTag)
                .map(t -> (UrlTag) t)
                .findFirst();

        assertTrue(optionalUrlTag.isPresent());
        assertEquals(targetString, optionalUrlTag.get().getUrl());
        nip01.close();
    }

    @Test
    public void testFiltersListReturnSameSingularEvent() throws IOException {
        System.out.println("testFiltersListReturnSameSingularEvent");

        String geoHashTagTarget = "geohash_tag-location_SameSingularEvent";
        GeohashTag geohashTag = new GeohashTag(geoHashTagTarget);

        String genericTagTarget = "generic-tag-value_SameSingularEvent";
        BaseTag genericTag = BaseTag.create("m", genericTagTarget);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());

        nip01.createTextNoteEvent(List.of(geohashTag, genericTag), "Multiple Filters").signAndSend(relays);

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
        String geoHashTagTarget1 = "geohash_tag-location-1";
        GeohashTag geohashTag1 = new GeohashTag(geoHashTagTarget1);
        String genericTagTarget1 = "generic-tag-value-1";
        BaseTag genericTag1 = BaseTag.create("m", genericTagTarget1);
        NIP01 nip01_1 = new NIP01(Identity.generateRandomIdentity());
        nip01_1.createTextNoteEvent(List.of(geohashTag1, genericTag1), "Multiple Filters 1").signAndSend(relays);

//    second event
        String geoHashTagTarget2 = "geohash_tag-location-2";
        GeohashTag geohashTag2 = new GeohashTag(geoHashTagTarget2);
        String genericTagTarget2 = "generic-tag-value-2";
        BaseTag genericTag2 = BaseTag.create("m", genericTagTarget2);
        NIP01 nip01_2 = new NIP01(Identity.generateRandomIdentity());
        nip01_2.createTextNoteEvent(List.of(geohashTag2, genericTag2), "Multiple Filters 2").signAndSend(relays);

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

        String geoHashTagTarget = "geohash_tag-location-DifferentTypesReturnSameEvent";
        GeohashTag geohashTag = new GeohashTag(geoHashTagTarget);

        String genericTagTarget = "generic-tag-value-DifferentTypesReturnSameEvent";
        BaseTag genericTag = BaseTag.create("m", genericTagTarget);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent(List.of(geohashTag, genericTag), "Multiple Filters").signAndSend(relays);

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

        Identity identity = Identity.generateRandomIdentity();
        var nip04 = new NIP04(identity, Identity.generateRandomIdentity().getPublicKey());
        var instance = nip04
                .createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...")
                .sign();

        var message = NIP04.decrypt(identity, instance.getEvent());

        assertEquals("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...", message);
    }

    @Test
    public void testNIP15CreateStallEvent() throws JsonProcessingException {
        System.out.println("testNIP15CreateStallEvent");

        Stall stall = createStall();
        var nip15 = new NIP15(Identity.create(PrivateKey.generateRandomPrivKey()));

        // Create and send the nostr event
        var instance = nip15.createCreateOrUpdateStallEvent(stall).sign();
        var signature = instance.getEvent().getSignature();
        assertNotNull(signature);

        // Fetch the content and compare with the above original
        var content = instance.getEvent().getContent();
        var expected = MAPPER_AFTERBURNER.readValue(content, Stall.class);

        assertEquals(expected, stall);
    }

    @Test
    public void testNIP15UpdateStallEvent() throws IOException {
        System.out.println("testNIP15UpdateStallEvent");

        var stall = createStall();
        var nip15 = new NIP15(Identity.create(PrivateKey.generateRandomPrivKey()));

        // Create and send the nostr event
        var instance = nip15.createCreateOrUpdateStallEvent(stall).sign();
        var signature = instance.getEvent().getSignature();
        assertNotNull(signature);

        var response = instance.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

        // Update the shipping
        var shipping = stall.getShipping();
        shipping.setCost(20.00f);

        EventNostr event = nip15.createCreateOrUpdateStallEvent(stall).sign();
        response = event.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

        nip15.close();
    }

    @Test
    public void testNIP15CreateProductEvent() throws IOException {

        System.out.println("testNIP15CreateProductEvent");

        // Create the stall object
        var stall = createStall();
        var nip15 = new NIP15(Identity.create(PrivateKey.generateRandomPrivKey()));

        // Create the product
        var product = createProduct(stall);

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        EventNostr event = nip15.createCreateOrUpdateProductEvent(product, categories).sign();
        var response = event.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

        nip15.close();
    }

    @Test
    public void testNIP15UpdateProductEvent() throws IOException {

        System.out.println("testNIP15UpdateProductEvent");

        // Create the stall object
        var stall = createStall();
        var nip15 = new NIP15(Identity.create(PrivateKey.generateRandomPrivKey()));

        // Create the product
        var product = createProduct(stall);

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        EventNostr event1 = nip15.createCreateOrUpdateProductEvent(product, categories).sign();
        var response = event1.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

        product.setDescription("Un nouveau bijou en or");
        categories.add("bagues");

        EventNostr event2 = nip15.createCreateOrUpdateProductEvent(product, categories).sign();
        response = event2.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip15.getEvent().getId(), ((OkMessage) response).getEventId());

        nip15.close();
    }

/*
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
        assertTrue(label.getAttributes().contains(new ElementAttribute("param0", "english")));
        assertTrue(label.getAttributes().contains(new ElementAttribute("param1", "Languages")));
    }
    @Test
    public void testNIP32CreateLabel2() {

        System.out.println("testNIP32CreateLabel2");

        var label = NIP32.createLabelTag("Languages", "english");

        assertEquals("l", label.getCode());
        assertTrue(label.getAttributes().contains(new ElementAttribute("param0", "english")));
        assertTrue(label.getAttributes().contains(new ElementAttribute("param1", "Languages")));
    }
*/

    @Test
    public void testNIP52CalendarTimeBasedEventEvent() throws IOException {
        System.out.println("testNIP52CalendarTimeBasedEventEvent");

        CalendarContent<BaseTag> calendarContent = new CalendarContent<>(
                new IdentifierTag("UUID-CalendarTimeBasedEventTest"),
                "Calendar Time-Based Event title",
                1716513986268L);

        calendarContent.setStartTzid("1687765220");
        calendarContent.setEndTzid("1687765230");
        calendarContent.addLabelNamespaceTags(List.of(new LabelNamespaceTag("audiospace")));
        calendarContent.addLabelTags(List.of(new LabelTag("english", "audiospace"), new LabelTag("mycenaean greek", "audiospace")));

        List<BaseTag> tags = new ArrayList<>();
        tags.add(new PubKeyTag(Identity.generateRandomIdentity().getPublicKey(),
                getRelayUri(),
                "ISSUER"));
        tags.add(new PubKeyTag(Identity.generateRandomIdentity().getPublicKey(),
                "",
                "COUNTERPARTY"));

        var nip52 = new NIP52(Identity.create(PrivateKey.generateRandomPrivKey()));
        EventNostr event = nip52.createCalendarTimeBasedEvent(tags, "content", calendarContent).sign();
        var response = event.setRelays(relays).send();
        assertInstanceOf(OkMessage.class, response);
        assertEquals(nip52.getEvent().getId(), ((OkMessage) response).getEventId());

        nip52.close();
    }

    @Test
    void testNIP57CreateZapRequestEvent() throws Exception {
        System.out.println("testNIP57CreateZapRequestEvent");

        var nip57 = new NIP57(Identity.generateRandomIdentity());
        final String ZAP_REQUEST_CONTENT = "zap request content";
        final Long AMOUNT = 1232456L;
        final String LNURL = "lnUrl";
        final String RELAYS_TAG = getRelayUri();

        var instance = nip57.createZapRequestEvent(
                AMOUNT,
                LNURL,
                List.of(new Relay(RELAYS_TAG)),
                ZAP_REQUEST_CONTENT,
                Identity.generateRandomIdentity().getPublicKey(),
                null,
                null).getEvent();

        instance.update();

        assertNotNull(instance.getId());
        assertNotNull(instance.getCreatedAt());
        assertNotNull(instance.getContent());
        assertNull(instance.getSignature());

        // TODO test with the tags

/*
        assertNotNull(instance.getZapRequest());
        assertNotNull(instance.getZapRequest().getRelaysTag());
        assertNotNull(instance.getZapRequest().getAmount());
        assertNotNull(instance.getZapRequest().getLnUrl());

        assertEquals(ZAP_REQUEST_CONTENT, instance.getContent());
        assertTrue(instance.getZapRequest().getRelaysTag().getRelays().stream()
                .anyMatch(relay -> relay.getUri().equals(RELAYS_TAG)));
        assertEquals(AMOUNT, instance.getZapRequest().getAmount());
        assertEquals(LNURL, instance.getZapRequest().getLnUrl());
*/

        final String bech32 = instance.toBech32();
        assertNotNull(bech32);
        assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
    }

    @Test
    void testNIP57CreateZapReceiptEvent() throws Exception {
        System.out.println("testNIP57CreateZapReceiptEvent");

        String zapRequestPubKeyTag = Identity.generateRandomIdentity().getPublicKey().toString();
        String zapRequestEventTag = Identity.generateRandomIdentity().getPublicKey().toString();
        String zapSender = Identity.generateRandomIdentity().getPublicKey().toString();
        PublicKey zapRecipient = Identity.generateRandomIdentity().getPublicKey();
        final String ZAP_RECEIPT_IDENTIFIER = "ipsum";
        final String ZAP_RECEIPT_RELAY_URI = getRelayUri();
        final String BOLT_11 = "bolt11";
        final String DESCRIPTION_SHA256 = "descriptionSha256";
        final String PRE_IMAGE = "preimage";
        var nip57 = new NIP57(Identity.generateRandomIdentity());

        var zapReceipt = new ZapReceipt(BOLT_11, DESCRIPTION_SHA256, PRE_IMAGE);

/*
        var instance = nip57.createZapReceiptEvent(
                        new PubKeyTag(new PublicKey(zapRequestPubKeyTag)),
                        new EventTag(zapRequestEventTag),
                        new PublicKey(zapSender),
                        zapRecipient,
                        new AddressTag(Kind.ZAP_RECEIPT.getValue(), new PublicKey(zapSender), new IdentifierTag(ZAP_RECEIPT_IDENTIFIER), new Relay(ZAP_RECEIPT_RELAY_URI)),
                        zapReceipt,
                        DESCRIPTION_SHA256)
                .getEvent();
*/
        final String ZAP_REQUEST_CONTENT = "zap request content";
        final Long AMOUNT = 1232456L;
        final String LNURL = "lnUrl";
        final String RELAYS_TAG = getRelayUri();

        var zapRequestEvent = nip57.createZapRequestEvent(
                AMOUNT,
                LNURL,
                List.of(new Relay(RELAYS_TAG)),
                ZAP_REQUEST_CONTENT,
                zapRecipient,
                null,
                null).getEvent();

        var instance = nip57.createZapReceiptEvent(
                zapRequestEvent,
                BOLT_11,
                PRE_IMAGE,
                zapRecipient).getEvent();

        instance.update();

        assertNotNull(instance.getId());
        assertNotNull(instance.getCreatedAt());
        assertNull(instance.getSignature());

        // TODO test with the tags
/*
        assertNotNull(instance.getZapReceipt());
        assertNotNull(instance.getZapReceipt().getBolt11());
        assertNotNull(instance.getZapReceipt().getDescriptionSha256());
        assertNotNull(instance.getZapReceipt().getPreimage());

        assertEquals(BOLT_11, instance.getZapReceipt().getBolt11());
        assertEquals(DESCRIPTION_SHA256, instance.getZapReceipt().getDescriptionSha256());
        assertEquals(PRE_IMAGE, instance.getZapReceipt().getPreimage());
*/

        final String bech32 = instance.toBech32();
        assertNotNull(bech32);
        assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
    }

    private static List<BaseTag> getBaseTags() {
        return new ArrayList<BaseTag>();
    }

    public static Stall createStall() {

        // Create the county list
        List<String> countries = new ArrayList<>();
        countries.add("France");
        countries.add("Canada");
        countries.add("Cameroun");

        // Create the shipping object
        var shipping = new Stall.Shipping();
        shipping.setCost(12.00f);
        shipping.setCountries(countries);
        shipping.setName("French Countries");

        // Create the stall object
        var stall = new Stall();
        stall.setCurrency("USD");
        stall.setDescription("This is a test stall");
        stall.setName("Maximus Primus");
        stall.setShipping(shipping);

        return stall;
    }

    public static Product createProduct(Stall stall) {

        // Create the product
        var product = new Product();
        product.setCurrency("USD");
        product.setDescription("Un bijou en or");
        product.setImages(new ArrayList<>());
        product.setName("Bague");
        product.setPrice(450.00f);
        product.setQuantity(4);
        List<Product.Spec> specs = new ArrayList<>();
        specs.add(new Product.Spec("couleur", "or"));
        specs.add(new Product.Spec("poids", "150g"));
        product.setSpecs(specs);
        product.setStall(stall);

        return product;
    }

    @Test
    public void testNIP01SendTextNoteEventVoteTag() throws IOException {
        System.out.println("testNIP01SendTextNoteEventVoteTag");

        Integer targetVote = 1;
        VoteTag voteTag = new VoteTag(targetVote);

        NIP01 nip01 = new NIP01(Identity.generateRandomIdentity());
        nip01.createTextNoteEvent(List.of(voteTag), "Vote Tag Test value testNIP01SendTextNoteEventVoteTag").signAndSend(relays);

        Filters filters = new Filters(
                new VoteTagFilter<>(new VoteTag(targetVote)));

        List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.contains(targetVote.toString())));

        nip01.close();
    }
}
