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

//    private final Client client;
//    private final Identity identity;

    public ClientTest() throws IOException, NostrException {
//        this.client = new Client("/relays.properties");
//        this.identity = new Identity("/profile.properties");
    }

    @Test
    public void testSend() {
        try {
            System.out.println("testSend");
            PublicKey publicKey = Identity.getInstance().getPublicKey();
            GenericMessage msg = new EventMessage(EntityFactory.Events.createTextNoteEvent(publicKey));
            Client.getInstance("/relays.properties").send(msg);
            assertTrue(true);
        } catch (IOException | NostrException ex) {
            fail(ex);
        }
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
