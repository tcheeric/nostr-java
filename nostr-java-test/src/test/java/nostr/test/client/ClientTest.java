package nostr.test.client;

import nostr.base.PrivateKey;
import nostr.client.Client;
import nostr.context.impl.DefaultRequestContext;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

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
        requestContext.setRelays(Map.of("My local test relay", "127.0.0.1:5555"));
        try {
            client = Client.getInstance().connect(requestContext);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void dispose() {
        try {
            this.client.disconnect();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        this.client = null;
        this.identity = null;
    }

    @Test
    public void testSend() throws TimeoutException {
        System.out.println("testSend");
        var event = EntityFactory.Events.createTextNoteEvent(identity.getPublicKey());
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);

        client.send(msg);

        assertTrue(true);
    }

    @Test
    public void disconnect() throws TimeoutException {
        System.out.println("disconnect");

        var relayCount = getRelayCount();
        Assertions.assertEquals(relayCount, client.getOpenConnectionsCount());
        client.disconnect();

        Assertions.assertEquals(0, client.getOpenConnectionsCount());
    }

    public int getRelayCount() {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("relays.properties")) {
            if (is != null) {
                properties.load(is);
            } else {
                throw new IOException("Cannot find relays.properties on the classpath");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.size();
    }
}
