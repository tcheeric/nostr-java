package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Regression tests for the close-vs-write race (spec-026 US3).
 *
 * <p>Spring's {@link org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator}
 * (Spring 6.2.x) overrides only {@code close(CloseStatus)} — guarded by a
 * {@code closeLock} that is <em>separate</em> from the {@code flushLock} guarding
 * {@code delegate.sendMessage}. It does <strong>not</strong> override the no-arg
 * {@code close()}, which falls through to {@code WebSocketSessionDecorator.close()}
 * → {@code delegate.close()} with no coordination at all. So a no-arg close sends a
 * CLOSE frame straight to the Tomcat delegate while a {@code sendMessage} flush is in
 * flight, tripping Tomcat's single-in-flight-write guard
 * ({@code IllegalStateException: Concurrent write operations are not permitted}).
 *
 * <p>The fix has two parts, both asserted here:
 * <ol>
 *   <li><b>Routing</b> — every close goes through {@code close(CloseStatus)}, never
 *       the no-arg {@code close()} (tests {@link #close_routesThroughCloseStatus_neverNoArgClose}
 *       and {@link #sendTimeout_routesCloseThroughCloseStatus_neverNoArgClose}).</li>
 *   <li><b>Serialisation</b> — {@code subscribe()} / {@code send()} writes hold the
 *       READ side of the client's {@code sessionGate} ({@code ReentrantReadWriteLock})
 *       and {@code close()} holds the WRITE side, so a close waits for all in-flight
 *       writes to drain and the delegate never sees a write and a close at once
 *       (test {@link #concurrentSubscribeAndClose_neverOverlapOnDelegate}).</li>
 * </ol>
 */
class NostrRelayClientCloseWriteRaceTest {

  private static final long TEST_AWAIT_TIMEOUT_MS = 5_000L;
  private static final String REQ = "[\"REQ\",\"sub-1\",{}]";

  // ---- Routing: close() must use close(CloseStatus), never the no-arg close() ----
  @Test
  void close_routesThroughCloseStatus_neverNoArgClose() throws Exception {
    AtomicBoolean isOpen = new AtomicBoolean(true);
    WebSocketSession raw = Mockito.mock(WebSocketSession.class);
    Mockito.when(raw.isOpen()).thenAnswer(inv -> isOpen.get());
    Mockito.doAnswer(inv -> { isOpen.set(false); return null; })
        .when(raw).close(any(CloseStatus.class));

    NostrRelayClient client =
        NostrRelayClient.forTestWithDecoratedSession(raw, TEST_AWAIT_TIMEOUT_MS);

    client.close();

    // The decorator's close(CloseStatus) calls super.close(status) → delegate.close(status).
    verify(raw, times(1)).close(any(CloseStatus.class));
    verify(raw, never()).close();
  }

  // ---- Routing: the send() timeout path must close via CloseStatus, not no-arg ----
  @Test
  void sendTimeout_routesCloseThroughCloseStatus_neverNoArgClose() throws Exception {
    AtomicBoolean isOpen = new AtomicBoolean(true);
    WebSocketSession raw = Mockito.mock(WebSocketSession.class);
    Mockito.when(raw.isOpen()).thenAnswer(inv -> isOpen.get());
    // sendMessage succeeds but no response is ever dispatched back, so send() times out.
    Mockito.doNothing().when(raw).sendMessage(any(TextMessage.class));
    Mockito.doAnswer(inv -> { isOpen.set(false); return null; })
        .when(raw).close(any(CloseStatus.class));

    // Short await timeout so the test does not block for the 5 s default.
    NostrRelayClient client =
        NostrRelayClient.forTestWithDecoratedSession(raw, 200L);

    assertThrows(RelayTimeoutException.class, () -> client.send(REQ));

    verify(raw, times(1)).close(any(CloseStatus.class));
    verify(raw, never()).close();
  }

  // ---- Guard: send() on an already-closed session must fail fast without
  //      reaching the delegate, so it never enters Tomcat's sendText →
  //      doClose-mid-write → "Concurrent write operations are not permitted"
  //      path (the per-subscription CLOSE-on-a-dying-connection trigger). ----
  @Test
  void send_onClosedSession_failsFastWithoutDelegateWrite() throws Exception {
    WebSocketSession raw = Mockito.mock(WebSocketSession.class);
    Mockito.when(raw.isOpen()).thenReturn(false); // session already closed/closing

    NostrRelayClient client =
        NostrRelayClient.forTestWithDecoratedSession(raw, TEST_AWAIT_TIMEOUT_MS);

    IOException ex = assertThrows(IOException.class, () -> client.send(REQ));
    assertTrue(ex.getMessage().contains("closed"),
        "expected a closed-session IOException, was: " + ex.getMessage());
    verify(raw, never()).sendMessage(any(TextMessage.class));
  }

  // ---- Serialisation: an in-flight subscribe write and a concurrent close
  //      must never overlap on the delegate. ----
  @Test
  void concurrentSubscribeAndClose_neverOverlapOnDelegate() throws Exception {
    AtomicBoolean isOpen = new AtomicBoolean(true);
    AtomicInteger delegateOps = new AtomicInteger();   // writes + closes in flight on the delegate
    AtomicInteger maxDelegateOps = new AtomicInteger();
    AtomicBoolean raceDetected = new AtomicBoolean(false);

    CountDownLatch sendInFlight = new CountDownLatch(1); // signalled while the send is parked
    CountDownLatch releaseSend = new CountDownLatch(1);  // test releases the parked send

    WebSocketSession raw = Mockito.mock(WebSocketSession.class);
    Mockito.when(raw.isOpen()).thenAnswer(inv -> isOpen.get());

    // First sendMessage parks in-flight (holding the decorator flushLock and this
    // client's sessionGate read lock) until the test releases it; later sends do
    // not park.
    AtomicBoolean parkedOnce = new AtomicBoolean(false);
    Answer<Void> sendAnswer = inv -> {
      enter(delegateOps, maxDelegateOps, raceDetected);
      try {
        if (parkedOnce.compareAndSet(false, true)) {
          sendInFlight.countDown();
          if (!releaseSend.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("parked send was never released");
          }
        }
      } finally {
        delegateOps.decrementAndGet();
      }
      return null;
    };
    Answer<Void> closeAnswer = inv -> {
      enter(delegateOps, maxDelegateOps, raceDetected);
      try {
        isOpen.set(false);
      } finally {
        delegateOps.decrementAndGet();
      }
      return null;
    };
    Mockito.doAnswer(sendAnswer).when(raw).sendMessage(any(TextMessage.class));
    Mockito.doAnswer(closeAnswer).when(raw).close();
    Mockito.doAnswer(closeAnswer).when(raw).close(any(CloseStatus.class));

    NostrRelayClient client =
        NostrRelayClient.forTestWithDecoratedSession(raw, TEST_AWAIT_TIMEOUT_MS);

    AtomicReference<Throwable> subscribeFailure = new AtomicReference<>();
    Thread subscriber = new Thread(() -> {
      try {
        client.subscribe(REQ, ignored -> {}, ignored -> {}, null);
      } catch (Throwable t) {
        subscribeFailure.set(t);
      }
    }, "subscriber");
    subscriber.setDaemon(true);
    subscriber.start();

    // Wait until the subscribe write is parked in flight on the delegate.
    assertTrue(sendInFlight.await(5, TimeUnit.SECONDS),
        "subscribe() never reached the delegate sendMessage");

    AtomicReference<Throwable> closeFailure = new AtomicReference<>();
    Thread closer = new Thread(() -> {
      try {
        client.close();
      } catch (Throwable t) {
        closeFailure.set(t);
      }
    }, "closer");
    closer.setDaemon(true);
    closer.start();

    // With the fix, close() blocks on the sessionGate WRITE lock, waiting for the
    // READ lock held by the parked subscribe to be released, so the delegate close
    // has NOT run yet — exactly one op (the write) in flight.
    awaitThreadParked(closer, 2_000);
    assertEquals(1, delegateOps.get(),
        "close() must not reach the delegate while a send is in flight on it");

    // Release the parked send; close can now proceed in serial order.
    releaseSend.countDown();
    subscriber.join(5_000);
    closer.join(5_000);

    assertFalse(raceDetected.get(),
        "delegate saw a write and a close concurrently — close-vs-write race");
    assertEquals(1, maxDelegateOps.get(),
        "at most one delegate write/close may be in flight at any time, was "
            + maxDelegateOps.get());
    assertTrue(subscribeFailure.get() == null
            || !(subscribeFailure.get() instanceof IllegalStateException),
        "subscribe() raised a concurrent-write IllegalStateException: " + subscribeFailure.get());
    assertEquals(null, closeFailure.get(),
        "close() failed: " + closeFailure.get());
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private static void enter(
      AtomicInteger delegateOps, AtomicInteger maxDelegateOps, AtomicBoolean raceDetected) {
    int now = delegateOps.incrementAndGet();
    maxDelegateOps.updateAndGet(prev -> Math.max(prev, now));
    if (now > 1) {
      raceDetected.set(true);
    }
  }

  /**
   * Poll until {@code thread} is parked (BLOCKED / WAITING / TIMED_WAITING) — i.e.
   * blocked acquiring the sessionGate write lock — or the timeout elapses.
   */
  private static void awaitThreadParked(Thread thread, long timeoutMs) throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
    while (System.nanoTime() < deadline) {
      Thread.State s = thread.getState();
      if (s == Thread.State.BLOCKED || s == Thread.State.WAITING
          || s == Thread.State.TIMED_WAITING) {
        return;
      }
      Thread.sleep(10);
    }
  }
}
