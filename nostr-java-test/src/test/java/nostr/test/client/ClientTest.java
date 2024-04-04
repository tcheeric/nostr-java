package nostr.test.client;

import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.client.Client;
import nostr.context.impl.DefaultRequestContext;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author squirrel
 */
class ClientTest {

    public ClientTest() {
    }

    @Test
    public void testSend() {
        System.out.println("testSend");
        Identity identity = Identity.getInstance(PrivateKey.generateRandomPrivKey());
        PublicKey publicKey = identity.getPublicKey();
        var event = EntityFactory.Events.createTextNoteEvent(publicKey);
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);

        var requestContext = new DefaultRequestContext();
        requestContext.setPrivateKey(identity.getPrivateKey().getRawData());
        requestContext.setRelays(Map.of("My local test relay", "localhost:5555"));

        Client.getInstance(requestContext).send(msg);

        assertTrue(true);
    }

    @Test
    public void disconnect() {
        System.out.println("disconnect");
        Identity identity = Identity.getInstance(PrivateKey.generateRandomPrivKey());
        PublicKey publicKey = identity.getPublicKey();
        var event = EntityFactory.Events.createTextNoteEvent(publicKey);
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);

        var requestContext = new DefaultRequestContext();
        requestContext.setPrivateKey(identity.getPrivateKey().getRawData());
        requestContext.setRelays(Map.of("My local test relay", "localhost:5555"));

        Client client = Client.getInstance(requestContext);
        Assertions.assertEquals(1, client.getOpenSessionsCount());
        client.send(msg);
        client.disconnect();

        Assertions.assertEquals(0, client.getOpenSessionsCount());

        event = EntityFactory.Events.createTextNoteEvent(publicKey);
        identity.sign(event);
        client.send(new EventMessage(event));

        assertTrue(true);
    }
}
