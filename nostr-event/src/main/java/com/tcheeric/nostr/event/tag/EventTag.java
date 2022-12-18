/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event.tag;

import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.Tag;
import com.tcheeric.nostr.event.BaseTag;
import com.tcheeric.nostr.event.Marker;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import com.tcheeric.nostr.event.impl.GenericEvent;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@Log
@EqualsAndHashCode(callSuper = true)
@Tag(code = "e", name = "event")
@ToString
public class EventTag extends BaseTag {

    @Key
    private GenericEvent relatedEvent;

    @Key
    private String recommendedRelayUrl;

    @NIPSupport(10)
    @Key
    private Marker marker;

    public EventTag(GenericEvent relatedEvent) {
        this(relatedEvent, null);
    }

    private EventTag(GenericEvent relatedEvent, String recommendedRelayUrl, Marker marker) {
        this.recommendedRelayUrl = recommendedRelayUrl;
        this.relatedEvent = relatedEvent;
        this.marker = marker;
    }

    private EventTag(GenericEvent relatedEvent, String recommendedRelayUrl) {
        this.recommendedRelayUrl = recommendedRelayUrl;
        this.relatedEvent = relatedEvent;
        this.marker = this.relatedEvent == null ? Marker.ROOT : Marker.REPLY;
    }
}
