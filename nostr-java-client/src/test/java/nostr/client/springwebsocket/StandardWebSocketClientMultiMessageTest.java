package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Tests that StandardWebSocketClient correctly accumulates multiple messages
 * before completing when a termination message (EOSE, OK) is received.
 */
class StandardWebSocketClientMultiMessageTest {

    @Test
    void testAccumulatesMessagesUntilEose() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        StandardWebSocketClient client = new StandardWebSocketClient(session, 5000, 100);

        // Simulate relay responses when send is called
        CountDownLatch sendLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            sendLatch.countDown();
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        // Start send in background thread
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

        // Wait for send to start
        assertTrue(sendLatch.await(1, TimeUnit.SECONDS), "Send should have started");
        Thread.sleep(50); // Small delay for send() to set up pendingRequest

        // Simulate EVENT message (not termination - should NOT complete)
        client.handleTextMessage(session, new TextMessage("[\"EVENT\",\"sub1\",{\"id\":\"abc\"}]"));

        // Small delay to ensure processing
        Thread.sleep(50);

        // Simulate EOSE message (termination - should complete)
        client.handleTextMessage(session, new TextMessage("[\"EOSE\",\"sub1\"]"));

        // Wait for send thread to complete
        sendThread.join(2000);
    }

    @Test
    void testCompletesImmediatelyOnOk() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        StandardWebSocketClient client = new StandardWebSocketClient(session, 5000, 100);

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

        // Simulate OK message (termination - should complete immediately)
        client.handleTextMessage(session, new TextMessage("[\"OK\",\"abc\",true,\"\"]"));

        sendThread.join(2000);
    }

    @Test
    void testEventWithoutEoseTimesOut() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);

        // Short timeout for this test
        StandardWebSocketClient client = new StandardWebSocketClient(session, 200, 50);

        CountDownLatch sendLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            sendLatch.countDown();
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        Thread sendThread = new Thread(() -> {
            try {
                List<String> result = client.send("[\"REQ\",\"sub1\",{}]");
                // Without EOSE, should timeout and return empty
                assertTrue(result.isEmpty(), "Should timeout and return empty list");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        sendThread.start();

        assertTrue(sendLatch.await(1, TimeUnit.SECONDS));
        Thread.sleep(50);

        // Simulate EVENT message but no EOSE
        client.handleTextMessage(session, new TextMessage("[\"EVENT\",\"sub1\",{\"id\":\"abc\"}]"));

        // Don't send EOSE - should timeout
        sendThread.join(1000);
    }
}
