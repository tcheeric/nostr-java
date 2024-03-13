package nostr.test.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nostr.event.impl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nostr.api.NIP01;
import nostr.api.NIP04;
import nostr.api.NIP15;
import nostr.api.NIP32;
import nostr.api.NIP44;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.impl.CreateOrUpdateStallEvent.Stall;
import nostr.event.impl.NostrMarketplaceEvent.Product.Spec;
import nostr.id.Identity;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class ApiEventTest {

    public static final String NOSTR_JAVA_PUBKEY = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";

    @Test
    public void testNIP01CreateTextNoteEvent() throws NostrException {
        System.out.println("testNIP01CreateTextNoteEvent");

        PublicKey publicKey = new PublicKey("");
        var recipient = NIP01.createPubKeyTag(publicKey);
        List<BaseTag> tags = new ArrayList<>();
        tags.add(recipient);

        var nip01 = new NIP01<TextNoteEvent>(Identity.getInstance());
		var instance = nip01.createTextNoteEvent(tags, "Hello simplified nostr-java!")
				.getEvent();
        instance.update();

        Assertions.assertNotNull(instance.getId());
        Assertions.assertNotNull(instance.getCreatedAt());
        Assertions.assertNull(instance.getSignature());

        final String bech32 = instance.toBech32();
        Assertions.assertNotNull(bech32);
        Assertions.assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
    }

    @Test
    public void testNIP01SendTextNoteEvent() {
        System.out.println("testNIP01SendTextNoteEvent");

        var nip01 = new NIP01<TextNoteEvent>(Identity.getInstance());
		var instance = nip01.createTextNoteEvent("Hello simplified nostr-java!")
        		.sign();

        var signature = instance.getEvent().getSignature();
        Assertions.assertNotNull(signature);
        instance.send();
    }

    @Test
    public void testNIP04SendDirectMessage() {
        System.out.println("testNIP04SendDirectMessage");

        PublicKey nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

        var nip04 = new NIP04<DirectMessageEvent>(Identity.getInstance(), nostr_java);
        var instance = nip04.createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...")
        		.sign();
        
        var signature = instance.getEvent().getSignature();
        Assertions.assertNotNull(signature);
        instance.send();
    }

    @Test
    public void testNIP44SendDirectMessage() {
        System.out.println("testNIP44SendDirectMessage");

        PublicKey nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

        var nip44 = new NIP44<EncryptedPayloadEvent>(Identity.getInstance(), nostr_java);

        var instance = nip44.createDirectMessageEvent(nostr_java, "Quand on n'a que l'amour pour tracer un chemin et forcer le destin...").sign();
        Assertions.assertNotNull(instance.getEvent().getSignature());
        instance.send();
    }

    @Test
    public void testNIP04EncryptDecrypt() throws NostrException {
        System.out.println("testNIP04EncryptDecrypt");

        var nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

        var nip04 = new NIP04<DirectMessageEvent>(Identity.getInstance(), nostr_java);
        var instance = nip04.createDirectMessageEvent("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...")
		        .encrypt()
		        .sign();
        var message = NIP04.decrypt(Identity.getInstance(), instance.getEvent());

        Assertions.assertEquals("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...", message);
    }

    @Test
    public void testNIP44EncryptDecrypt() {
        System.out.println("testNIP44EncryptDecrypt");

        var nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

        var nip44 = new NIP44<EncryptedPayloadEvent>(Identity.getInstance(), nostr_java);

        var instance = nip44.createDirectMessageEvent(nostr_java, "Quand on n'a que l'amour pour tracer un chemin et forcer le destin...").sign();
        var message = NIP44.decrypt(Identity.getInstance(), instance.getEvent());

        Assertions.assertEquals("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...", message);
    }

    @Test
    public void testNIP15CreateStallEvent() throws JsonProcessingException {
        System.out.println("testNIP15CreateStallEvent");

        Stall stall = createStall();
        var nip15 = new NIP15<>(Identity.getInstance());

        // Create and send the nostr event
        var instance = nip15.createCreateOrUpdateStallEvent(stall).sign();
        var signature = instance.getEvent().getSignature();
        Assertions.assertNotNull(signature);

        // Fetch the content and compare with the above original
        var content = instance.getEvent().getContent();
        ObjectMapper mapper = new ObjectMapper();
        var expected = mapper.readValue(content, Stall.class);

        Assertions.assertEquals(expected, stall);
    }

    @Test
    public void testNIP15UpdateStallEvent() {
        System.out.println("testNIP15UpdateStallEvent");

        var stall = createStall();
        var nip15 = new NIP15<>(Identity.getInstance());

        // Create and send the nostr event
        var instance = nip15.createCreateOrUpdateStallEvent(stall).sign();
        var signature = instance.getEvent().getSignature();
        Assertions.assertNotNull(signature);
        nip15.send();

        // Update the shipping
        var shipping = stall.getShipping();
        shipping.setCost(20.00f);
        nip15.createCreateOrUpdateStallEvent(stall).sign().send();
    }

    @Test
    public void testNIP15CreateProductEvent() {

        System.out.println("testNIP15CreateProductEvent");

        // Create the stall object
        var stall = createStall();
        var nip15 = new NIP15<>(Identity.getInstance());

        // Create the product
        var product = createProduct(stall);

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        nip15.createCreateOrUpdateProductEvent(product, categories).sign().send();
    }

    @Test
    public void testNIP15UpdateProductEvent() {

        System.out.println("testNIP15UpdateProductEvent");

        // Create the stall object
        var stall = createStall();
        var nip15 = new NIP15<>(Identity.getInstance());

        // Create the product
        var product = createProduct(stall);

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        var instance = nip15.createCreateOrUpdateProductEvent(product, categories);
        nip15.sign().send();

        product.setDescription("Un nouveau bijou en or");
        categories.add("bagues");

        nip15.sign().send();
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

    private NostrMarketplaceEvent.Product createProduct(Stall stall) {

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
}
