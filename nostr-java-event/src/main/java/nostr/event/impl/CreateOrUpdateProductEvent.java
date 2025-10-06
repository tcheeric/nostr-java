package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.Product;
import nostr.event.json.codec.EventEncodingException;

/**
 * @author eric
 */
@Event(name = "Create Or Update Product Event", nip = 15)
@NoArgsConstructor
public class CreateOrUpdateProductEvent extends MerchantEvent<Product> {

  public CreateOrUpdateProductEvent(PublicKey sender, List<BaseTag> tags, @NonNull String content) {
    super(sender, 30_018, tags, content);
  }

  public Product getProduct() {
    try {
      return IEvent.MAPPER_BLACKBIRD.readValue(getContent(), Product.class);
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to parse product content", ex);
    }
  }

  protected Product getEntity() {
    return getProduct();
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.PRODUCT_CREATE_OR_UPDATE.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.PRODUCT_CREATE_OR_UPDATE.getValue());
    }
  }

  protected void validateContent() {
    super.validateContent();

    try {
      Product product = getProduct();

      if (product.getName() == null || product.getName().isEmpty()) {
        throw new AssertionError("Invalid `content`: `name` field is required.");
      }

      if (product.getCurrency() == null || product.getCurrency().isEmpty()) {
        throw new AssertionError("Invalid `content`: `currency` field is required.");
      }

      if (product.getPrice() == null) {
        throw new AssertionError("Invalid `content`: `price` field is required.");
      }
    } catch (EventEncodingException e) {
      throw new AssertionError("Invalid `content`: Must be a valid Product JSON object.", e);
    }
  }
}
