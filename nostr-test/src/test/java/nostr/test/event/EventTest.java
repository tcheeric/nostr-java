
package nostr.test.event;

import crypto.bech32.Bech32;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Wallet;
import nostr.test.EntityFactory;
import java.io.IOException;
import nostr.base.Bech32Prefix;
import nostr.util.NostrException;
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
        try {
            System.out.println("testCreateTextNoteEvent");
            PublicKey publicKey = this.wallet.getProfile().getPublicKey();
            GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
            Assertions.assertNotNull(instance.getId());
            Assertions.assertNotNull(instance.getCreatedAt());
            Assertions.assertNull(instance.getSignature());
            final String bech32 = instance.toBech32();
            Assertions.assertNotNull(bech32);
            Assertions.assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }    
}
