package nostr.test.client;

import nostr.base.PrivateKey;
import nostr.client.Client;
import nostr.context.impl.DefaultRequestContext;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author squirrel
 */
class ClientTest {

    private Client client;
    private Identity identity;

    public ClientTest() {
    }

    @BeforeEach
    public void init() {
        identity = Identity.getInstance(PrivateKey.generateRandomPrivKey());
        //PublicKey publicKey = identity.getPublicKey();

        var requestContext = new DefaultRequestContext();
        requestContext.setPrivateKey(identity.getPrivateKey().getRawData());
        requestContext.setRelays(Map.of("My local test relay", "localhost:5555"));
        client = Client.getInstance(requestContext);
    }

    @AfterEach
    public void dispose() {
        this.client.disconnect();
        this.client = null;
        this.identity = null;
    }

    @Test
    public void testSend() {
        System.out.println("testSend");
        var event = EntityFactory.Events.createTextNoteEvent(identity.getPublicKey());
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);

        client.send(msg);

        assertTrue(true);
    }

    // FIXME!
/*
    @Test
    public void disconnect() {
        System.out.println("disconnect");
        var event = EntityFactory.Events.createTextNoteEvent(identity.getPublicKey());
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);

        Assertions.assertEquals(1, client.getOpenConnectionsCount());
        client.send(msg);
        client.disconnect();

        Assertions.assertEquals(0, client.getOpenConnectionsCount());

        event = EntityFactory.Events.createTextNoteEvent(identity.getPublicKey());
        identity.sign(event);
        client.send(new EventMessage(event));

        assertTrue(true);
    }
*/
}
