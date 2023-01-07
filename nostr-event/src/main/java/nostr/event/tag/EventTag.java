/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import nostr.base.annotation.Key;
import nostr.event.BaseTag;
import nostr.event.Marker;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import lombok.ToString;
import nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "e", name = "event")
public class EventTag extends BaseTag {

    @Key
    private GenericEvent relatedEvent;

    @Key
    private String recommendedRelayUrl;

    @Key(nip = 10)
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
