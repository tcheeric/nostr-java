
package nostr.test.event;

import nostr.base.NostrException;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Wallet;
import nostr.test.EntityFactory;
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
