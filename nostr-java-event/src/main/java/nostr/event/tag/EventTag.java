/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.Marker;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Tag(code = "e", name = "event")
public class EventTag extends BaseTag {

    @Key
    private String idEvent;

    @Key
    private String recommendedRelayUrl;

    @Key(nip = 10)
    private Marker marker;

    public EventTag(String idEvent) {
        this.recommendedRelayUrl = null;
        this.idEvent = idEvent;
        this.marker = this.idEvent == null ? Marker.ROOT : Marker.REPLY;
    }
}
