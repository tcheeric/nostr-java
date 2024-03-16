package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.api.*;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.impl.CreateOrUpdateStallEventNick.Stall;
import nostr.event.impl.ProductNick;
import nostr.event.impl.ProductNick.Spec;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author eric
 */
public class ApiEventTestNick {

  public static final String NOSTR_JAVA_PUBKEY = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";

  @Test
  public void testNIP01CreateTextNoteEvent() throws NostrException {
    System.out.println("testNIP01CreateTextNoteEvent");

    PublicKey publicKey = new PublicKey("");
    var recipient = NIP01Nick.createPubKeyTag(publicKey);
    List<BaseTag> tags = new ArrayList<>();
    tags.add(recipient);

    var instance = NIP01Nick.createTextNoteEvent(tags, "Hello simplified nostr-java!");
    instance.update();

    Assertions.assertNotNull(instance.getId());
    Assertions.assertNotNull(instance.getCreatedAt());
    Assertions.assertNull(instance.getSignature());

    final String bech32 = instance.toBech32();
    Assertions.assertNotNull(bech32);
    Assertions.assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
  }

  @Test
  public void testNIP99CreateClassifiedListEvent() throws NostrException {
    System.out.println("testNIP99CreateClassifiedListEvent");

    PublicKey publicKey = new PublicKey(NOSTR_JAVA_PUBKEY);
    var recipient = NIP01Nick.createPubKeyTag(publicKey);
    List<BaseTag> tags = new ArrayList<>();
    tags.add(recipient);
//        TODO: below
//        tags.add(NIP01Nick.creat)

    var instance = new NIP99(Identity.getInstance()).createClassifiedListingEvent(publicKey, tags, "Classified Listing Creation Event Content", "Classified Listing Creation Event TITLE", "Classified Listing Creation Summary", "Prosiltion", List.of(new String[]{"price", "$666", "BTC"}), "BTC");
//    instance.update();

//    Assertions.assertNotNull(instance.get());
//    Assertions.assertNotNull(instance.getCreatedAt());
//    Assertions.assertNull(instance.getSignature());
//
//    final String bech32 = instance.toBech32();
//    Assertions.assertNotNull(bech32);
//    Assertions.assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
  }

  @Test
  public void testNIP01SendTextNoteEvent() {
    System.out.println("testNIP01SendTextNoteEvent");

    var instance = NIP01Nick.createTextNoteEvent("Hello simplified nostr-java!");

    var signature = NostrNick.sign(instance);
    Assertions.assertNotNull(signature);
    NostrNick.send(instance);
  }

  @Test
  public void testNIP15CreateStallEvent() throws JsonProcessingException {
    System.out.println("testNIP15CreateStallEvent");

    Stall stall = createStall();

    // Create and send the nostr event
    var instance = NIP15Nick.createCreateOrUpdateStallEvent(stall);
    var signature = NostrNick.sign(instance);
    Assertions.assertNotNull(signature);
    NostrNick.send(instance);

    // Fetch the content and compare with the above original
    var content = instance.getContent();
    ObjectMapper mapper = new ObjectMapper();
    var expected = mapper.readValue(content, Stall.class);

    Assertions.assertEquals(expected, stall);
  }

  @Test
  public void testNIP15UpdateStallEvent() {
    System.out.println("testNIP15UpdateStallEvent");

    var stall = createStall();

    // Create and send the nostr event
    var instance = NIP15Nick.createCreateOrUpdateStallEvent(stall);
    var signature = NostrNick.sign(instance);
    Assertions.assertNotNull(signature);
    NostrNick.send(instance);

    // Update the shipping
    var shipping = stall.getShipping();
    shipping.setCost(20.00f);
    instance = NIP15Nick.createCreateOrUpdateStallEvent(stall);
    NostrNick.sign(instance);
    NostrNick.send(instance);
  }

  @Test
  public void testNIP15CreateProductEvent() {

    System.out.println("testNIP15CreateProductEvent");

    // Create the stall object
    var stall = createStall();

    // Create the product
    var product = createProduct(stall);

    List<String> categories = new ArrayList<>();
    categories.add("bijoux");
    categories.add("Hommes");

    var instance = NIP15Nick.createCreateOrUpdateProductEvent(product, categories);
    NostrNick.sign(instance);
    NostrNick.send(instance);
  }

  @Test
  public void testNIP15UpdateProductEvent() {

    System.out.println("testNIP15UpdateProductEvent");

    // Create the stall object
    var stall = createStall();

    // Create the product
    var product = createProduct(stall);

    List<String> categories = new ArrayList<>();
    categories.add("bijoux");
    categories.add("Hommes");

    var instance = NIP15Nick.createCreateOrUpdateProductEvent(product, categories);
    NostrNick.sign(instance);
    NostrNick.send(instance);

    product.setDescription("Un nouveau bijou en or");
    categories.add("bagues");

    NostrNick.sign(instance);
    NostrNick.send(instance);
  }

  @Test
  public void testNIP32CreateNameSpace() {

    System.out.println("testNIP32CreateNameSpace");

    var langNS = NIP32.createNameSpaceTag("Languages");

    Assertions.assertEquals("L", langNS.getCode());
    Assertions.assertEquals(1, langNS.getAttributes().size());
    Assertions.assertEquals("Languages", langNS.getAttributes().iterator().next().getValue());
  }

  @Test
  public void testNIP32CreateLabel1() {

    System.out.println("testNIP32CreateLabel1");

    var label = NIP32.createLabelTag("Languages", "english");

    Assertions.assertEquals("l", label.getCode());
    Assertions.assertEquals(2, label.getAttributes().size());
    Assertions.assertTrue(label.getAttributes().contains(new ElementAttribute("param0", "english", 32)));
    Assertions.assertTrue(label.getAttributes().contains(new ElementAttribute("param1", "Languages", 32)));
  }

  @Test
  public void testNIP32CreateLabel2() {

    System.out.println("testNIP32CreateLabel2");

    var metadata = new HashMap<String, Object>();
    metadata.put("article", "the");
    var label = NIP32.createLabelTag("Languages", "english", metadata);

    Assertions.assertEquals("l", label.getCode());
    Assertions.assertEquals(3, label.getAttributes().size());
    Assertions.assertTrue(label.getAttributes().contains(new ElementAttribute("param0", "english", 32)));
    Assertions.assertTrue(label.getAttributes().contains(new ElementAttribute("param1", "Languages", 32)));
    Assertions.assertTrue(label.getAttributes().contains(new ElementAttribute("param2", "{\\\"article\\\":\\\"the\\\"}", 32)), "{\\\"article\\\":\\\"the\\\"}");
  }

  private Stall createStall() {

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

  private ProductNick createProduct(Stall stall) {

    // Create the product
    var product = new ProductNick();
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
}
