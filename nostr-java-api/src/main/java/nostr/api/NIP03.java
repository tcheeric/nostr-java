/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
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
    public static OtsEvent createOtsEvent(String ots, String content) {
        return new OtsEventFactory(ots, content).create();
    }
    
    /**
     * Create a NIP03 OTS event
     * @param tags the note's tags
     * @param ots the OpenTimestamp attestation
     * @param content the note's content
     * @return the OTS event
     */
    public static OtsEvent createOtsEvent(List<BaseTag> tags, String ots, String content) {
        return new OtsEventFactory(tags, ots, content).create();
    }
}
