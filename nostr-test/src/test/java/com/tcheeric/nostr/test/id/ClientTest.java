package com.tcheeric.nostr.test.id;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.BaseMessage;
import com.tcheeric.nostr.event.message.EventMessage;
import com.tcheeric.nostr.id.Client;
import com.tcheeric.nostr.id.Wallet;
import com.tcheeric.nostr.test.EntityFactory;
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
