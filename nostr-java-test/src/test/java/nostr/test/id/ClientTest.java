package nostr.test.id;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import nostr.base.PublicKey;
import nostr.event.impl.GenericMessage;
import nostr.event.message.EventMessage;
import nostr.id.Client;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
class ClientTest {

    public ClientTest() throws IOException, NostrException {
    }

    @Test
    public void testSend() {
        System.out.println("testSend");
        PublicKey publicKey = Identity.getInstance().getPublicKey();
        GenericMessage msg = new EventMessage(EntityFactory.Events.createTextNoteEvent(publicKey));
        Client.getInstance().send(msg);
        assertTrue(true);
    }

//    @Test
//    public void testSendFail() throws Exception {
//        System.out.println("testSendFail");
//        PublicKey publicKey = client.getIdentity().getProfile().getPublicKey();
//        BaseMessage msg = EventMessage.builder().event(EntityFactory.Events.createTextNoteEvent(publicKey)).build();
//        
//        System.out.println("Sleeping for 33 seconds...");
//        Thread.sleep(33000);
//        
//        IOException thrown = Assertions.assertThrows(IOException.class,
//                () -> {
//                    this.client.send(msg);
//                }
//        );
//        assertNotNull(thrown);
//    }
}
