package nostr.test.client;

import lombok.extern.java.Log;
import nostr.base.PrivateKey;
import nostr.client.Client;
import nostr.context.impl.DefaultRequestContext;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author squirrel
 */
@Log
class ClientTest {

    private Client client;
    private Identity identity;

    public ClientTest() {
    }

    @BeforeEach
    public void init() {
        log.info("init");
        identity = Identity.create(PrivateKey.generateRandomPrivKey());

        DefaultRequestContext requestContext = new DefaultRequestContext();
        requestContext.setPrivateKey(identity.getPrivateKey().getRawData());
        //requestContext.setRelays(Map.of("My local test relay", "ws://localhost:5555"));
        Properties loadedProperties = getRelayProperties();
        requestContext.setRelays(convertPropertiesToMap(loadedProperties));
        try {
            client = Client.getInstance().connect(requestContext);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    //@AfterEach
    public void dispose() {
        log.info("dispose");
        try {
            this.client.disconnect();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        this.client = null;
        this.identity = null;
    }

    @Test
    public void testSend() {
        log.info("testSend");
        var event = EntityFactory.Events.createTextNoteEvent(identity.getPublicKey());
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);
        assertDoesNotThrow(() -> {
            client.send(msg);
        });
        assertTrue(true);
    }

    @Test
    public void disconnect() {
        log.info("disconnect");

        int relayCount = getRelayCount();
        assertEquals(relayCount, client.getOpenConnectionsCount());
        assertDoesNotThrow(() -> {
            client.disconnect();
        });
        assertEquals(0, client.getOpenConnectionsCount());

        assertDoesNotThrow(() -> {
            client.disconnect(); // if all connections are closed, trying to disconnect again wont throw error
        });
    }

/*
    @Test
    public void testNip42() {
        System.out.println("testNip42");

        var rcpt = Identity.generateRandomIdentity().getPublicKey();
        var sender = identity.getPublicKey();
        var event = EntityFactory.Events.createDirectMessageEvent(sender, rcpt, "Hello, World!");
        identity.sign(event);
        BaseMessage msg = new EventMessage(event);
        assertDoesNotThrow(() -> {
          client.send(msg);

          var filters = Filters.builder().kinds(new KindList(4)).authors(new PublicKeyList(sender)).build();
          msg = new ReqMessage("testNip42_" + sender.toString(), filters);
          client.send(msg);
        });
    }
*/

    private Properties getRelayProperties() {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("relays.properties")) {
            if (is != null) {
                properties.load(is);
            } else {
                throw new IOException("Cannot find relays.properties on the classpath.");
            }
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
        return properties;
    }

    private int getRelayCount() {
        return getRelayProperties().size();
    }

    public static Map<String, String> convertPropertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<>();

        // Iterate over the properties and put each entry into the map
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String name : propertyNames) {
            map.put(name, properties.getProperty(name));
        }

        return map;
    }

}
