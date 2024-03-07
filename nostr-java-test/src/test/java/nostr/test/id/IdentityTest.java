package nostr.test.id;

import nostr.base.PublicKey;
import nostr.event.tag.DelegationTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.id.IdentityHelper;
import nostr.test.EntityFactory;
import java.io.IOException;
import nostr.base.PrivateKey;
import nostr.crypto.bech32.Bech32;
import nostr.event.impl.DirectMessageEvent;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class IdentityTest {

    public IdentityTest() {
    }

    @Test
    public void testSignEvent() {
        System.out.println("testSignEvent");
        PublicKey publicKey = Identity.getInstance().getPublicKey();
        GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
        Identity.getInstance().sign(instance);
        Assertions.assertNotNull(instance.getSignature());
    }

    @Test
    public void testSignDelegationTag() {
        System.out.println("testSignDelegationTag");
        PublicKey publicKey = Identity.getInstance().getPublicKey();
        DelegationTag delegationTag = new DelegationTag(publicKey, null);
        Identity.getInstance().sign(delegationTag);
        Assertions.assertNotNull(delegationTag.getSignature());
    }
    
    
/*
    @Test
    public void testDecryptMessage() {
        try {
            System.out.println("testDecryptMessage");
            var senderPublicKey = Identity.getInstance().getPublicKey();
            
            PrivateKey rcptSecKey = new PrivateKey(NostrUtil.hexToBytes(Bech32.fromBech32("nsec13sntjjh35dd4u3lwy42lnpszydmkwar708y3jzwxr937fy2q73hsmvez4z")));
            PublicKey rcptPubKey = new PublicKey("edd898fc2817ee64f7ee1941d193d53c2daa77db4b8409240565fc9644626878");

            final DirectMessageEvent dmEvent = EntityFactory.Events.createDirectMessageEvent(senderPublicKey, rcptPubKey, "Hello uq7yfx3l!");

            new IdentityHelper(Identity.getInstance()).encryptDirectMessage(dmEvent);

            var rcptId = new Identity(rcptSecKey);
            var msg = new IdentityHelper(rcptId).decryptMessage(dmEvent.getContent(), dmEvent.getPubKey());
            
            Assertions.assertEquals("Hello uq7yfx3l!", msg);
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }
*/

}
