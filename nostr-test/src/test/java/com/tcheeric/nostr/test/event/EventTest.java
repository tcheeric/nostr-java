
package com.tcheeric.nostr.test.event;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.impl.GenericEvent;
import com.tcheeric.nostr.id.Wallet;
import com.tcheeric.nostr.test.EntityFactory;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class EventTest {

    private final Wallet wallet;

    public EventTest() throws IOException, NostrException {
        this.wallet = new Wallet();
    }

    @Test
    public void testCreateTextNoteEvent() {
        System.out.println("CreateTextNoteEvent");
        PublicKey publicKey = this.wallet.getProfile().getPublicKey();
        GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
        Assertions.assertNotNull(instance.getId());
        Assertions.assertNotNull(instance.getCreatedAt());
        Assertions.assertNull(instance.getSignature());
    }

}
