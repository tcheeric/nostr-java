package nostr.api.unit;

import nostr.api.NIP15;
import nostr.event.entities.Product;
import nostr.event.entities.Stall;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.IdentifierTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NIP15Test {

  @Test
  public void testCreateProductEvent() {
    Identity sender = Identity.generateRandomIdentity();
    NIP15 nip15 = new NIP15(sender);
    Product product = new Product();
    product.setName("item");
    product.setStall(new Stall());
    product.setCurrency("USD");
    product.setPrice(1f);
    product.setQuantity(1);
    nip15.createCreateOrUpdateProductEvent(product, List.of("tag"));
    GenericEvent event = nip15.getEvent();
    assertNotNull(event.getId());
    IdentifierTag idTag = (IdentifierTag) event.getTags().get(0);
    assertNotNull(idTag.getUuid());
  }
}
