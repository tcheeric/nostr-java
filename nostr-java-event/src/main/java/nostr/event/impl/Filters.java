
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Key;
import nostr.event.BaseEvent;
import nostr.event.json.deserializer.CustomGenericTagQueryListDeserializer;
import nostr.event.json.serializer.CustomGenericTagQueryListSerializer;
import nostr.event.json.serializer.CustomIdEventListSerializer;
import nostr.event.list.EventList;
import nostr.event.list.GenericTagQueryList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class Filters extends BaseEvent {

    @Key
    @JsonProperty("ids")
    @JsonSerialize(using=CustomIdEventListSerializer.class)
    private EventList events;

    @Key
    @JsonProperty("authors")
    private PublicKeyList authors;

    @Key
    private KindList kinds;

    @Key
    @JsonProperty("#e")
    @JsonSerialize(using=CustomIdEventListSerializer.class)
    private EventList referencedEvents;

    @Key
    @JsonProperty("#p")
    private PublicKeyList referencePubKeys;

    @Key
    private Long since;

    @Key
    private Long until;

    @Key
    private Integer limit;

    @Key(nip = 12)
    @JsonSerialize(using=CustomGenericTagQueryListSerializer.class)    
    @JsonDeserialize(using=CustomGenericTagQueryListDeserializer.class)    
    private GenericTagQueryList genericTagQueryList;

    @Override
    public String toBech32() {
        throw new UnsupportedOperationException("This operation is not supported.");
    }

    @JsonIgnore
    @Override
    public Integer getNip() {
        return 1;
    }
}
