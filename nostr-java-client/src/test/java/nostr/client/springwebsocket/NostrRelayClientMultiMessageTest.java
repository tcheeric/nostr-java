package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Tests that NostrRelayClient correctly accumulates multiple messages
 * before completing when a termination message (EOSE, OK) is received.
 */
class NostrRelayClientMultiMessageTest {

    @Test
    void testAccumulatesMessagesUntilEose() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        NostrRelayClient client = new NostrRelayClient(session, 5000);

        CountDownLatch sendLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            sendLatch.countDown();
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        Thread sendThread = new Thread(() -> {
            try {
                List<String> result = client.send("[\"REQ\",\"sub1\",{}]");
                assertEquals(2, result.size(), "Should receive EVENT + EOSE");
                assertTrue(result.get(0).contains("EVENT"), "First message should be EVENT");
                assertTrue(result.get(1).contains("EOSE"), "Second message should be EOSE");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        sendThread.start();

        assertTrue(sendLatch.await(1, TimeUnit.SECONDS), "Send should have started");
        Thread.sleep(50);

        client.handleTextMessage(session, new TextMessage("[\"EVENT\",\"sub1\",{\"id\":\"abc\"}]"));
        Thread.sleep(50);
        client.handleTextMessage(session, new TextMessage("[\"EOSE\",\"sub1\"]"));

        sendThread.join(2000);
    }

    @Test
    void testCompletesImmediatelyOnOk() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        NostrRelayClient client = new NostrRelayClient(session, 5000);

        CountDownLatch sendLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            sendLatch.countDown();
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        Thread sendThread = new Thread(() -> {
            try {
                List<String> result = client.send("[\"EVENT\",{\"id\":\"abc\"}]");
                assertEquals(1, result.size(), "Should receive just OK");
                assertTrue(result.get(0).contains("OK"), "Message should be OK");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        sendThread.start();

        assertTrue(sendLatch.await(1, TimeUnit.SECONDS));
        Thread.sleep(50);

        client.handleTextMessage(session, new TextMessage("[\"OK\",\"abc\",true,\"\"]"));

        sendThread.join(2000);
    }

    @Test
    void testEventWithoutEoseThrowsRelayTimeout() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        NostrRelayClient client = new NostrRelayClient(session, 200);

        CountDownLatch sendLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            sendLatch.countDown();
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        AtomicReference<Throwable> caught = new AtomicReference<>();
        Thread sendThread = new Thread(() -> {
            try {
                client.send("[\"REQ\",\"sub1\",{}]");
            } catch (Exception e) {
                caught.set(e);
            }
        });
        sendThread.start();

        assertTrue(sendLatch.await(1, TimeUnit.SECONDS));
        Thread.sleep(50);

        client.handleTextMessage(session, new TextMessage("[\"EVENT\",\"sub1\",{\"id\":\"abc\"}]"));

        sendThread.join(1000);
        assertInstanceOf(RelayTimeoutException.class, caught.get(),
            "Should throw RelayTimeoutException on timeout");
    }
}
