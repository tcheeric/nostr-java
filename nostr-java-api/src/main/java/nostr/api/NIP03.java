/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP03Impl;
import nostr.base.IEvent;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP03<T extends GenericEvent> extends EventNostr<T> {

    public NIP03(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create a NIP03 OTS event
     * @param referencedEvent the referenced event
     * @param ots the full content of an .ots file containing at least one Bitcoin attestation
     * @param alt the note's content
     * @return an OTS event
     */
    public NIP03 createOtsEvent(@NonNull IEvent referencedEvent, @NonNull String ots, @NonNull String alt) {
        var factory = new NIP03Impl.OtsEventFactory(getSender(), referencedEvent, ots, alt);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }
}
