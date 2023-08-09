package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import nostr.api.NIP01;
import nostr.api.NIP04;
import nostr.api.NIP15;
import nostr.api.Nostr;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.impl.CreateOrUpdateStallEvent;
import nostr.event.impl.CreateOrUpdateStallEvent.Stall;
import nostr.event.impl.NostrMarketplaceEvent;
import nostr.event.impl.NostrMarketplaceEvent.Product.Spec;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        var recipient = new NIP01.PubKeyTagFactory(publicKey).create();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(recipient);

        var instance = new NIP01.TextNoteEventFactory(tags, "Hello simplified nostr-java!").create();
        instance.update();

        Assertions.assertNotNull(instance.getId());
        Assertions.assertNotNull(instance.getCreatedAt());
        Assertions.assertNull(instance.getSignature());

        final String bech32 = instance.toBech32();
        Assertions.assertNotNull(bech32);
        Assertions.assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
    }

    @Test
    public void testNIP01SendTextNoteEvent() throws NostrException {
        System.out.println("testNIP01SendTextNoteEvent");

        var instance = new NIP01.TextNoteEventFactory("Hello simplified nostr-java!").create();

        var signature = Nostr.sign(instance);
        Assertions.assertNotNull(signature);
        Nostr.send(instance);
    }

    @Test
    public void testNIP04SendDirectMessage() throws NostrException {
        System.out.println("testNIP04SendDirectMessage");

        PublicKey nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

        var instance = new NIP04.DirectMessageEventFactory(nostr_java, "Quand on n'a que l'amour pour tracer un chemin et forcer le destin...").create();
        var signature = Nostr.sign(instance);
        Assertions.assertNotNull(signature);
        Nostr.send(instance);
    }

    @Test
    public void testNIP04EncryptDecrypt() throws NostrException {
        System.out.println("testNIP04EncryptDecrypt");

        PublicKey nostr_java = new PublicKey(NOSTR_JAVA_PUBKEY);

        var instance = new NIP04.DirectMessageEventFactory(nostr_java, "Quand on n'a que l'amour pour tracer un chemin et forcer le destin...").create();
        NIP04.encrypt(instance);
        Nostr.sign(instance);
        var message = NIP04.decrypt(instance);

        Assertions.assertEquals("Quand on n'a que l'amour pour tracer un chemin et forcer le destin...", message);
    }

    @Test
    public void testNIP15CreateStallEvent() throws NostrException, JsonProcessingException {
        System.out.println("testNIP15CreateStallEvent");

        Stall stall = createStall();

        // Create and send the nostr event
        var instance = new NIP15.CreateOrUpdateStallEventFactory(stall).create();
        var signature = Nostr.sign(instance);
        Assertions.assertNotNull(signature);
        Nostr.send(instance);

        // Fetch the content and compare with the above original
        var content = instance.getContent();
        ObjectMapper mapper = new ObjectMapper();
        var expected = mapper.readValue(content, Stall.class);

        Assertions.assertEquals(expected, stall);
    }

    @Test
    public void testNIP15UpdateStallEvent() throws NostrException, JsonProcessingException {
        System.out.println("testNIP15UpdateStallEvent");

        var stall = createStall();

        // Create and send the nostr event
        var instance = new NIP15.CreateOrUpdateStallEventFactory(stall).create();
        var signature = Nostr.sign(instance);
        Assertions.assertNotNull(signature);
        Nostr.send(instance);

        // Update the shipping
        var shipping = stall.getShipping();
        shipping.setCost(20.00f);
        instance = new NIP15.CreateOrUpdateStallEventFactory(stall).create();
        Nostr.sign(instance);
        Nostr.send(instance);
    }

    @Test
    public void testNIP15CreateProductEvent() throws NostrException {

        System.out.println("testNIP15CreateProductEvent");

        // Create the stall object
        var stall = createStall();

        // Create the product
        var product = createProduct(stall);

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        var instance = new NIP15.CreateOrUpdateProductEventFactory(product, categories).create();
        Nostr.sign(instance);
        Nostr.send(instance);
    }

    @Test
    public void testNIP15UpdateProductEvent() throws NostrException {

        System.out.println("testNIP15UpdateProductEvent");

        // Create the stall object
        var stall = createStall();

        // Create the product
        var product = createProduct(stall);

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        var instance = new NIP15.CreateOrUpdateProductEventFactory(product, categories).create();
        Nostr.sign(instance);
        Nostr.send(instance);
        
        product.setDescription("Un nouveau bijou en or");
        categories.add("bagues");
        
        Nostr.sign(instance);
        Nostr.send(instance);
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
