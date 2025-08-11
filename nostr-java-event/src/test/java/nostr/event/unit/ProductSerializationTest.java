package nostr.event.unit;

import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.entities.Product;
import nostr.event.entities.Stall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;
import static org.junit.jupiter.api.Assertions.*;

public class ProductSerializationTest {

    @Test
    void specSerialization() throws Exception {
        Product.Spec spec = new Product.Spec("color", "blue");
        String json = MAPPER_BLACKBIRD.writeValueAsString(spec);
        JsonNode node = MAPPER_BLACKBIRD.readTree(json);
        assertEquals("color", node.get("key").asText());
        assertEquals("blue", node.get("value").asText());
    }

    @Test
    void productSerialization() throws Exception {
        Product product = new Product();
        Stall stall = new Stall();
        product.setStall(stall);
        product.setName("item");
        product.setCurrency("USD");
        product.setPrice(1f);
        product.setQuantity(1);
        product.setSpecs(List.of(new Product.Spec("size", "M")));

        JsonNode node = MAPPER_BLACKBIRD.readTree(product.value());

        assertTrue(node.has("id"));
        assertEquals("item", node.get("name").asText());
        assertEquals("USD", node.get("currency").asText());
        assertEquals(1f, node.get("price").floatValue());
        assertEquals(1, node.get("quantity").asInt());
        assertTrue(node.has("stall"));
        assertEquals(stall.getId(), node.get("stall").get("id").asText());
        assertTrue(node.has("specs"));
        assertTrue(node.get("specs").isArray());
        JsonNode specNode = node.get("specs").get(0);
        assertEquals("size", specNode.get("key").asText());
        assertEquals("M", specNode.get("value").asText());
    }
}
