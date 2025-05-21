package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.Product;

import java.util.List;

/**
 * @author eric
 */
@Event(name = "Create Or Update Product Event", nip = 15)
@NoArgsConstructor
public class CreateOrUpdateProductEvent extends MerchantEvent<Product> {

    public CreateOrUpdateProductEvent(PublicKey sender, List<BaseTag> tags, @NonNull String content) {
        super(sender, 30_018, tags, content);
    }

    @SneakyThrows
    public Product getProduct() {
        return IEvent.MAPPER_AFTERBURNER.readValue(getContent(), Product.class);
    }

    protected Product getEntity() {
        return getProduct();
    }
}
