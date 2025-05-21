package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nostr.event.json.serializer.ProductSerializer;
import nostr.event.json.serializer.SpecSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = ProductSerializer.class)
public class Product extends NIP15Content.MerchantContent {

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
