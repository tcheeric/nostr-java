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
    
    public static OtsEvent createOtsEvent(String ots, String content) {
        return new OtsEventFactory(ots, content).create();
    }
    
    public static OtsEvent createOtsEvent(List<BaseTag> tags, String ots, String content) {
        return new OtsEventFactory(tags, ots, content).create();
    }
}
