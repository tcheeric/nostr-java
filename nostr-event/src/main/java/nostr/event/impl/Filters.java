/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.impl;

import nostr.event.BaseEvent;
import nostr.event.list.EventList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.annotation.Key;
import nostr.base.annotation.NIPSupport;
import nostr.event.list.GenericTagQueryList;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Log
@ToString
@NIPSupport
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

    @Key
    @NIPSupport(12)
    private GenericTagQueryList genericTagQueryList;

    @Override
    public String toBech32() {
        throw new UnsupportedOperationException("This operation is not supported.");
    }
}
