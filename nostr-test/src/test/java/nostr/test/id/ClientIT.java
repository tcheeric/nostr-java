package nostr.test.id;

import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.id.Client;
import nostr.id.Wallet;
import nostr.test.EntityFactory;
import java.io.IOException;
import nostr.util.NostrException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author squirrel
 */
class ClientIT {

    private final Client client;

    public ClientIT() throws IOException, NostrException {
        this.client = new Client("TestClient", new Wallet());
    }

    @Test
    void testSend() {
        try {
            System.out.println("testSend");
            PublicKey publicKey = client.getWallet().getProfile().getPublicKey();
            BaseMessage msg = EventMessage.builder().event(EntityFactory.Events.createTextNoteEvent(publicKey)).build();
            this.client.send(msg);
            assertTrue(true);
        } catch (Exception ex) {
            fail(ex);
        }
    }

//    @Test
//    public void testSendFail() throws Exception {
//        System.out.println("testSendFail");
//        PublicKey publicKey = client.getWallet().getProfile().getPublicKey();
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
