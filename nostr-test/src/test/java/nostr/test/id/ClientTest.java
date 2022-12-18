package nostr.test.id;

import nostr.base.NostrException;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.id.Client;
import nostr.id.Wallet;
import nostr.test.EntityFactory;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class ClientTest {

    private final Client client;

    public ClientTest() throws IOException, NostrException {
        this.client = new Client("TestClient", new Wallet());
    }

    @Test
    public void testSend() {
        try {
            System.out.println("testSend");
            PublicKey publicKey = client.getWallet().getProfile().getPublicKey();
            BaseMessage msg = EventMessage.builder().event(EntityFactory.Events.createTextNoteEvent(publicKey)).build();
            this.client.send(msg);
            Assertions.assertTrue(true);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testSendFail() throws Exception {
        System.out.println("testSendFail");
        PublicKey publicKey = client.getWallet().getProfile().getPublicKey();
        BaseMessage msg = EventMessage.builder().event(EntityFactory.Events.createTextNoteEvent(publicKey)).build();
        
        System.out.println("Sleeping for 33 seconds...");
        Thread.sleep(33000);
        
        IOException thrown = Assertions.assertThrows(IOException.class,
                () -> {
                    this.client.send(msg);
                }
        );
        Assertions.assertNotNull(thrown);
    }
}
