/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event.impl;

import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.event.BaseEvent;
import com.tcheeric.nostr.event.list.EventList;
import com.tcheeric.nostr.event.list.KindList;
import com.tcheeric.nostr.event.list.PublicKeyList;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import com.tcheeric.nostr.base.annotation.NIPSupport;

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
}
