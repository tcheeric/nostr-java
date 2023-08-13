/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.NIP03.OtsEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.OtsEvent;

/**
 *
 * @author eric
 */
public class NIP03 extends Nostr {
    
    /**
     * Create a NIP03 OTS event
     * @param ots the OpenTimestamp attestation
     * @param content the note's content
     * @return an OTS event
     */
    public static OtsEvent createOtsEvent(@NonNull String ots, @NonNull String content) {
        return new OtsEventFactory(ots, content).create();
    }
    
    /**
     * Create a NIP03 OTS event
     * @param tags the note's tags
     * @param ots the OpenTimestamp attestation
     * @param content the note's content
     * @return the OTS event
     */
    public static OtsEvent createOtsEvent(@NonNull List<BaseTag> tags, @NonNull String ots, @NonNull String content) {
        return new OtsEventFactory(tags, ots, content).create();
    }
}
