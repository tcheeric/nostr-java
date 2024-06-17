
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.event.Kind;
import nostr.event.json.deserializer.CustomGenericTagQueryDeserializer;
import nostr.event.json.serializer.CustomGenericTagQuerySerializer;
import nostr.event.json.serializer.CustomIdEventListSerializer;

import java.util.List;

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
    @JsonSerialize(using=CustomGenericTagQuerySerializer.class)
    @JsonDeserialize(using=CustomGenericTagQueryDeserializer.class)
    private GenericTagQuery genericTagQuery;
}
