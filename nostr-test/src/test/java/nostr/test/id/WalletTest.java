package nostr.test.id;

import nostr.base.PublicKey;
import nostr.event.tag.DelegationTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Wallet;
import nostr.test.EntityFactory;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class WalletTest {

    private final Wallet wallet;

    public WalletTest() throws IOException, NostrException {
        this.wallet = new Wallet();
    }

    @Test
    public void testSignEvent() {
        try {
            System.out.println("testSignEvent");
            PublicKey publicKey = this.wallet.getProfile().getPublicKey();
            GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
            this.wallet.sign(instance);
            Assertions.assertNotNull(instance.getSignature());
        } catch (IntrospectionException ex) {
            Assertions.fail(ex);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
            Assertions.fail(ex);
        } catch (Exception ex) {
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
        } catch (IntrospectionException ex) {
            Assertions.fail(ex);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
            Assertions.fail(ex);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }
    
}
