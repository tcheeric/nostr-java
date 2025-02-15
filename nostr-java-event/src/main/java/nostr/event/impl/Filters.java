package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.event.Kind;
import nostr.event.json.serializer.CustomIdEventListSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Filters {

    @Key
    @JsonProperty("ids")
    @JsonSerialize(using=CustomIdEventListSerializer.class)
    private List<GenericEvent> events;

    @Key
    @JsonProperty("authors")
    private List<PublicKey> authors;

    @Key
    private List<Kind> kinds;

    @Key
    @JsonProperty("#e")
    @JsonSerialize(using=CustomIdEventListSerializer.class)
    private List<GenericEvent> referencedEvents;

    @Key
    @JsonProperty("#p")
    private List<PublicKey> referencePubKeys;

    @Key
    private Long since;

    @Key
    private Long until;

    @Key
    private Integer limit;

    @Key(nip = 12)
    @Setter(AccessLevel.NONE)
    private Map<String, List<String>> genericTagQuery;

    public void setUntil(@NonNull Long until) {
        if (until < 0) {
            throw new IllegalArgumentException("'until' filter cannot be negative.");
        }
        this.until = until;
    }

    public void setSince(@NonNull Long since) {
        if (since < 0) {
            throw new IllegalArgumentException("'since' filter cannot be negative.");
        }
        this.since = since;
    }

    @JsonAnyGetter
    public Map<String, List<String>> getGenericTagQuery() {
        return genericTagQuery;
    }

    @JsonAnySetter
    public void setGenericTagQuery(String key, List<String> value) {
        this.genericTagQuery = Optional.ofNullable(genericTagQuery).orElse(new HashMap<>());
        this.genericTagQuery.put(key, value);
    }
}
