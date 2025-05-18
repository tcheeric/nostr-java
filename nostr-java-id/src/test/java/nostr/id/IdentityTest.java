package nostr.id;

import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.DelegationTag;
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
        Identity identity = Identity.generateRandomIdentity();
        PublicKey publicKey = identity.getPublicKey();
        GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
        identity.sign(instance);
        Assertions.assertNotNull(instance.getSignature());
    }

    @Test
    public void testSignDelegationTag() {
        System.out.println("testSignDelegationTag");
        Identity identity = Identity.generateRandomIdentity();
        PublicKey publicKey = identity.getPublicKey();
        DelegationTag delegationTag = new DelegationTag(publicKey, null);
        identity.sign(delegationTag);
        Assertions.assertNotNull(delegationTag.getSignature());
    }


}
