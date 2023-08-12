/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.impl.NIP09.DeletionEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.DeletionEvent;

/**
 *
 * @author eric
 */
public class NIP09 extends Nostr {

    public static DeletionEvent createDeletionEvent(List<BaseTag> tags) {
        return new DeletionEventFactory(tags).create();
    }
}
