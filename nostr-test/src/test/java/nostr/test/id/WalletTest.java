package nostr.test.id;

import nostr.base.PublicKey;
import nostr.event.tag.DelegationTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import java.io.IOException;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class WalletTest {

    private final Identity wallet;

    public WalletTest() throws IOException, NostrException {
        this.wallet = new Identity();
    }

    @Test
    public void testSignEvent() {
        try {
            System.out.println("testSignEvent");
            PublicKey publicKey = this.wallet.getProfile().getPublicKey();
            GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
            this.wallet.sign(instance);
            Assertions.assertNotNull(instance.getSignature());
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testSignDelegationTag() {
        try {
            System.out.println("testSignDelegationTag");
            PublicKey publicKey = this.wallet.getProfile().getPublicKey();
            DelegationTag delegationTag = new DelegationTag(publicKey, null);
            this.wallet.sign(delegationTag);
            Assertions.assertNotNull(delegationTag.getSignature());
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }
    
}
