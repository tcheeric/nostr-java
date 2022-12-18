package com.tcheeric.nostr.test.id;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.tag.DelegationTag;
import com.tcheeric.nostr.event.impl.GenericEvent;
import com.tcheeric.nostr.id.Wallet;
import com.tcheeric.nostr.test.EntityFactory;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
