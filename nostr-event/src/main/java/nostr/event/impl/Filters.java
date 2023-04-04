
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.annotation.Key;
import nostr.event.BaseEvent;
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
@Log
public class Filters extends BaseEvent {

    @Key(name = "ids")
    private EventList events;

    @Key(name = "authors")
    private PublicKeyList authors;

    @Key
    private KindList kinds;

    @Key(name = "#e")
    private EventList referencedEvents;

    @Key(name = "#p")
    private PublicKeyList referencePubKeys;

    @Key
    private Long since;

    @Key
    private Long until;

    @Key
    private Integer limit;

    @Key(nip = 12)
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
