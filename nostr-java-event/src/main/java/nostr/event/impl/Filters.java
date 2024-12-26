
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.event.Kind;
import nostr.event.json.deserializer.CustomGenericTagQueryDeserializer;
import nostr.event.json.serializer.CustomIdEventListSerializer;

import java.util.List;
import java.util.Map;

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
    @JsonDeserialize(using= CustomGenericTagQueryDeserializer.class)
    private Map<String,Object> genericTagQuery;
    @JsonAnyGetter
    public Map<String,Object> getGenericTagQuery() {
        return genericTagQuery;
    }

    @JsonAnySetter
    public void setGenericTagQuery(Map<String,Object> genericTagQuery) {
        this.genericTagQuery = genericTagQuery;
    }
}
