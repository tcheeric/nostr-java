package nostr.event.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.BaseTag;
import nostr.event.Kind;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Create or update a stall", nip = 15)
public class CreateOrUpdateStallEvent extends NostrMarketplaceEvent {

    public CreateOrUpdateStallEvent(PublicKey sender, List<BaseTag> tags, @NonNull Stall stall) {
        super(sender, Kind.STALL_CREATE_OR_UPDATE.getValue(), tags, stall);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = false)
    public static class Stall extends AbstractEventContent<CreateOrUpdateStallEvent> {

        @JsonProperty
        private final String id;

        @JsonProperty
        private String name;

        @JsonProperty
        private String description;

        @JsonProperty
        private String currency;

        @JsonProperty
        private Shipping shipping;

        public Stall() {
            this.id = UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).substring(0, 64);
        }

        @Data
        public static class Shipping {

            @JsonProperty
            private final String id;

            @JsonProperty
            private String name;

            @JsonProperty
            private Float cost;

            @JsonProperty
            private List<String> countries;

            public Shipping() {
                this.countries = new ArrayList<>();
                this.id = UUID.randomUUID().toString();
            }
        }
    }
}
