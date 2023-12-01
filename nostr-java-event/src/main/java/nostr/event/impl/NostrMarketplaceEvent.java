package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.BaseTag;
import nostr.event.IContent;
import nostr.event.impl.CreateOrUpdateStallEvent.Stall;
import nostr.event.json.serializer.ProductSerializer;
import nostr.event.json.serializer.SpecSerializer;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
public abstract class NostrMarketplaceEvent extends ParameterizedReplaceableEvent {

    protected NostrMarketplaceEvent() {
        super();
    }
    public NostrMarketplaceEvent(PublicKey sender, Integer kind, List<BaseTag> tags, IContent content) {
        super(sender, kind, tags, content.toString());
    }

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = false)
    @JsonSerialize(using = ProductSerializer.class)
    public static class Product extends AbstractEventContent<NostrMarketplaceEvent> {

        @JsonProperty
        private final String id;

        @JsonProperty
        private Stall stall;

        @JsonProperty
        private String name;

        @JsonProperty
        private String description;

        @JsonProperty
        private List<String> images;

        @JsonProperty
        private String currency;

        @JsonProperty
        private Float price;

        @JsonProperty
        private int quantity;

        @JsonProperty
        private List<Spec> specs;

        public Product() {
            this.specs = new ArrayList<>();
            this.images = new ArrayList<>();
            this.id = UUID.randomUUID().toString();
        }

        @Data
        @AllArgsConstructor
        @JsonSerialize(using = SpecSerializer.class)
        public static class Spec {

            @JsonProperty
            private final String key;

            @JsonProperty
            private final String value;
        }
    }

}
