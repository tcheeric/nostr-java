package nostr.api.unit;

import nostr.api.NostrSpringWebSocketClient;
import nostr.api.WebSocketClientHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.*;

public class NostrSpringWebSocketClientTest {

    private static class TestClient extends NostrSpringWebSocketClient {
        @Override
        protected WebSocketClientHandler newWebSocketClientHandler(String relayName, String relayUri) {
            try {
                return createHandler(relayName, relayUri);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static WebSocketClientHandler createHandler(String name, String uri) throws Exception {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        WebSocketClientHandler handler = (WebSocketClientHandler) unsafe.allocateInstance(WebSocketClientHandler.class);

        Field relayName = WebSocketClientHandler.class.getDeclaredField("relayName");
        relayName.setAccessible(true);
        relayName.set(handler, name);

        Field relayUri = WebSocketClientHandler.class.getDeclaredField("relayUri");
        relayUri.setAccessible(true);
        relayUri.set(handler, uri);

        Field eventClient = WebSocketClientHandler.class.getDeclaredField("eventClient");
        eventClient.setAccessible(true);
        eventClient.set(handler, null);

        Field requestClientMap = WebSocketClientHandler.class.getDeclaredField("requestClientMap");
        requestClientMap.setAccessible(true);
        requestClientMap.set(handler, new ConcurrentHashMap<>());

        return handler;
    }

    @Test
    void testMultipleSubscriptionsDoNotOverwriteHandlers() throws Exception {
        NostrSpringWebSocketClient client = new TestClient();

        Field field = NostrSpringWebSocketClient.class.getDeclaredField("clientMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, WebSocketClientHandler> map = (Map<String, WebSocketClientHandler>) field.get(client);

        map.put("relayA", createHandler("relayA", "ws://a"));
        map.put("relayB", createHandler("relayB", "ws://b"));

        Method method = NostrSpringWebSocketClient.class.getDeclaredMethod("createRequestClient", String.class);
        method.setAccessible(true);

        method.invoke(client, "sub1");
        assertEquals(4, map.size());
        WebSocketClientHandler handlerA1 = map.get("relayA:sub1");
        WebSocketClientHandler handlerB1 = map.get("relayB:sub1");
        assertNotNull(handlerA1);
        assertNotNull(handlerB1);

        method.invoke(client, "sub2");
        assertEquals(6, map.size());
        assertSame(handlerA1, map.get("relayA:sub1"));
        assertSame(handlerB1, map.get("relayB:sub1"));
        assertNotNull(map.get("relayA:sub2"));
        assertNotNull(map.get("relayB:sub2"));
    }
}
