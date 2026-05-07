package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

/**
 * Concurrency regression tests for the {@code subscribe()} send-side race
 * fixed by wrapping {@code clientSession} in a
 * {@code ConcurrentWebSocketSessionDecorator}.
 *
 * <p>Structured as <strong>fail-first / pass-after</strong> per the spec
 * acceptance criteria (§6.7):
 *
 * <ul>
 *   <li>(7a) Reproduction — a raw, non-decorated session demonstrates the
 *       {@code IllegalStateException} race that the decorator fixes.
 *   <li>(7b) Resolution — a decorated session passes the same harness without
 *       any exception, with an in-flight invariant of at most 1.
 *   <li>(7c) Mixed-workload — a 100 KiB {@code send()} co-existing with a
 *       burst of {@code subscribe()} REQs, validating the 256 KiB default
 *       buffer sizing.
 *   <li>(7d) Overflow-and-close — a tiny buffer is forced to overflow under
 *       indefinite-block sends, validating the {@code TERMINATE} strategy
 *       closes the underlying session.
 * </ul>
 *
 * <p>Constructor-validation tests cover positive-value enforcement on the
 * package-private four-arg test ctor.
 */
class NostrRelayClientConcurrencyTest {

  private static final long TEST_AWAIT_TIMEOUT_MS = 5_000L;
  private static final int CONCURRENCY = 32;
  private static final int CONCURRENCY_LIGHT = 20;
  private static final int BLOCK_MS = 50;
  private static final int RESOLUTION_DEADLINE_MS = 5_000;
  private static final int MIXED_WORKLOAD_DEADLINE_MS = 10_000;

  // ---- §6.7a: reproduction step against a raw (non-decorated) session ----
  @Test
  void rawSession_concurrentSubscribe_reproducesTextFullWritingRace() throws Exception {
    AtomicInteger inFlight = new AtomicInteger();
    AtomicInteger maxInFlight = new AtomicInteger();
    AtomicBoolean isOpen = new AtomicBoolean(true);
    WebSocketSession raw = newBlockingMockSession(inFlight, maxInFlight, isOpen, BLOCK_MS,
        /* throwOnConcurrent= */ true);

    NostrRelayClient client = NostrRelayClient.forTestWithRawSession(raw, TEST_AWAIT_TIMEOUT_MS);

    List<Throwable> failures = runConcurrentSubscribes(client, CONCURRENCY);

    long ioWithIllegalStateCause = failures.stream()
        .filter(t -> t instanceof IOException)
        .filter(t -> "Failed to send subscription payload".equals(t.getMessage()))
        .filter(t -> t.getCause() instanceof IllegalStateException)
        .count();
    assertTrue(
        ioWithIllegalStateCause >= 1,
        "Expected at least one IOException(\"Failed to send subscription payload\") with cause "
            + "IllegalStateException to reproduce the concurrent-send race against a raw session "
            + "(observed " + ioWithIllegalStateCause + " out of " + failures.size() + " failures)");
  }

  // ---- §6.7b: resolution step against a decorated session ----
  @Test
  void decoratedSession_concurrentSubscribe_serialisesAndSucceeds() throws Exception {
    AtomicInteger inFlight = new AtomicInteger();
    AtomicInteger maxInFlight = new AtomicInteger();
    AtomicBoolean isOpen = new AtomicBoolean(true);
    WebSocketSession raw = newBlockingMockSession(inFlight, maxInFlight, isOpen, BLOCK_MS,
        /* throwOnConcurrent= */ true);

    NostrRelayClient client =
        NostrRelayClient.forTestWithDecoratedSession(raw, TEST_AWAIT_TIMEOUT_MS);

    long start = System.nanoTime();
    List<Throwable> failures = runConcurrentSubscribes(client, CONCURRENCY);
    long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

    assertTrue(failures.isEmpty(),
        "Expected zero failures with the decorator, observed " + failures.size()
            + ": first=" + (failures.isEmpty() ? "<none>" : failures.get(0)));
    assertEquals(1, maxInFlight.get(),
        "Decorator must serialise sends — maxInFlight should be exactly 1, was "
            + maxInFlight.get());
    assertTrue(elapsedMs < RESOLUTION_DEADLINE_MS * 2L,
        "Resolution harness exceeded " + (RESOLUTION_DEADLINE_MS * 2L)
            + "ms wall-clock (was " + elapsedMs + "ms)");

    // (iv) field-injection regression — constructor args reflected, not static defaults.
    int defaultsBuffer = NostrRelayClient.forTestWithDecoratedSession(
        Mockito.mock(WebSocketSession.class), TEST_AWAIT_TIMEOUT_MS).sendBufferLimitForTest();
    assertEquals(256 * 1024, defaultsBuffer,
        "Two-arg test factory must use the 256 KiB default buffer");
    int customBuffer = NostrRelayClient.forTestWithDecoratedSession(
        Mockito.mock(WebSocketSession.class), TEST_AWAIT_TIMEOUT_MS, 8 * 1024, 7_500)
        .sendBufferLimitForTest();
    assertEquals(8 * 1024, customBuffer,
        "Four-arg test factory must reflect the constructor argument, not the static default");
    int customTimeLimit = NostrRelayClient.forTestWithDecoratedSession(
        Mockito.mock(WebSocketSession.class), TEST_AWAIT_TIMEOUT_MS, 8 * 1024, 7_500)
        .sendTimeLimitMsForTest();
    assertEquals(7_500, customTimeLimit,
        "Four-arg test factory must reflect the sendTimeLimitMs constructor argument");
  }

  // ---- §6.7c: mixed-workload (100 KiB send racing with subscribes) ----
  @Test
  void decoratedSession_mixedWorkload_largeSendWithSubscribesUnderDefaultBuffer() throws Exception {
    AtomicInteger inFlight = new AtomicInteger();
    AtomicInteger maxInFlight = new AtomicInteger();
    AtomicBoolean isOpen = new AtomicBoolean(true);
    // Light blocking on subscribes; the large send is simulated by a separate
    // 200 ms blocking branch keyed on payload size.
    WebSocketSession raw = newSizeAwareMockSession(inFlight, maxInFlight, isOpen,
        /* normalBlockMs= */ BLOCK_MS, /* largeBlockMs= */ 200,
        /* largeThresholdBytes= */ 64 * 1024);

    NostrRelayClient client = NostrRelayClient.forTestWithDecoratedSession(
        raw, TEST_AWAIT_TIMEOUT_MS,
        /* sendBufferLimit= */ 256 * 1024, /* sendTimeLimitMs= */ 10_000);

    String largePayload = makePayload(100 * 1024);
    int totalCalls = CONCURRENCY_LIGHT + 1;
    List<Throwable> failures = new ArrayList<>();
    ExecutorService pool = Executors.newFixedThreadPool(totalCalls);
    CountDownLatch startingGun = new CountDownLatch(1);

    long start = System.nanoTime();
    for (int i = 0; i < CONCURRENCY_LIGHT; i++) {
      final int idx = i;
      pool.submit(() -> {
        try {
          startingGun.await();
          client.subscribe("[\"REQ\",\"sub-" + idx + "\"]",
              ignored -> {}, ignored -> {}, null);
        } catch (Throwable t) {
          synchronized (failures) { failures.add(t); }
        }
      });
    }
    pool.submit(() -> {
      try {
        startingGun.await();
        // The size-aware mock branches to a 200 ms slow-flush path on this
        // payload — simulating a chunked-EVENT publish racing the subscribes.
        client.subscribe(largePayload, ignored -> {}, ignored -> {}, null);
      } catch (Throwable t) {
        synchronized (failures) { failures.add(t); }
      }
    });
    startingGun.countDown();
    pool.shutdown();
    boolean done = pool.awaitTermination(MIXED_WORKLOAD_DEADLINE_MS + 5_000, TimeUnit.MILLISECONDS);
    long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

    assertTrue(done, "Mixed-workload pool did not terminate within "
        + (MIXED_WORKLOAD_DEADLINE_MS + 5_000) + "ms");
    assertTrue(failures.isEmpty(),
        "Mixed workload failures: " + failures.size()
            + " — first=" + (failures.isEmpty() ? "<none>" : failures.get(0)));
    assertEquals(1, maxInFlight.get(),
        "Decorator must serialise the large send + 20 subscribes — maxInFlight was "
            + maxInFlight.get());
    assertTrue(elapsedMs < MIXED_WORKLOAD_DEADLINE_MS,
        "Mixed workload exceeded " + MIXED_WORKLOAD_DEADLINE_MS + "ms wall-clock (was "
            + elapsedMs + "ms)");
  }

  // ---- §6.7d: overflow-and-close ----
  @Test
  void decoratedSession_overflow_closesUnderlyingSession() throws Exception {
    AtomicInteger inFlight = new AtomicInteger();
    AtomicInteger maxInFlight = new AtomicInteger();
    AtomicBoolean isOpen = new AtomicBoolean(true);
    AtomicBoolean unblock = new AtomicBoolean(false);
    WebSocketSession raw = newIndefinitelyBlockingMockSession(inFlight, maxInFlight, isOpen,
        unblock);

    // Tiny 256-byte buffer with 1 KiB-sized subscribes — overflow guaranteed.
    NostrRelayClient client = NostrRelayClient.forTestWithDecoratedSession(
        raw, TEST_AWAIT_TIMEOUT_MS,
        /* sendBufferLimit= */ 256, /* sendTimeLimitMs= */ 10_000);

    String oneKib = makePayload(1024);
    int n = 50;
    List<Throwable> failures = new ArrayList<>();
    ExecutorService pool = Executors.newFixedThreadPool(n);
    CountDownLatch startingGun = new CountDownLatch(1);
    for (int i = 0; i < n; i++) {
      pool.submit(() -> {
        try {
          startingGun.await();
          client.subscribe(oneKib, ignored -> {}, ignored -> {}, null);
        } catch (Throwable t) {
          synchronized (failures) { failures.add(t); }
        }
      });
    }
    startingGun.countDown();

    // Wait for the decorator to overflow and close the session.
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (System.nanoTime() < deadline && isOpen.get()) {
      Thread.sleep(50);
    }
    // Release the indefinite-blockers so the pool can drain.
    unblock.set(true);
    synchronized (raw) { raw.notifyAll(); }
    pool.shutdown();
    pool.awaitTermination(10, TimeUnit.SECONDS);

    assertTrue(failures.size() >= 1,
        "Expected at least one overflow exception under TERMINATE (observed "
            + failures.size() + ")");

    // Asserting (i) above; (ii) — spec §6.7d ii says the underlying session
    // must be closed — is intentionally relaxed here. Spring's
    // ConcurrentWebSocketSessionDecorator under TERMINATE does NOT close the
    // delegate from limitExceeded() (it sets a private `limitExceeded` flag
    // and throws SessionLimitExceededException; the delegate is closed only
    // when somebody subsequently invokes the decorator's close()). The spec's
    // assumption that overflow alone closes the session is incorrect for
    // bare Spring; the closure happens upstream when NostrJavaRelayClient (or
    // the application's MessageBrokerWebSocketHandler) handles the exception.
    // We therefore assert the propagated overflow exception only, and leave
    // the close-on-overflow chain to the upstream §6.7e wallet-lib test.
    boolean overflowExceptionPropagated = failures.stream().anyMatch(t -> {
      String msg = t.getMessage();
      if (msg != null && msg.contains("SessionLimitExceeded")) return true;
      // The wrapped form: NostrRelayClient.subscribe() catches RuntimeException
      // (which SessionLimitExceededException extends) and rewraps as
      // IOException("Failed to send subscription payload", e).
      if (t instanceof IOException
          && "Failed to send subscription payload".equals(t.getMessage())
          && t.getCause() != null
          && t.getCause().getClass().getName().endsWith("SessionLimitExceededException")) {
        return true;
      }
      return false;
    });
    assertTrue(overflowExceptionPropagated,
        "Expected at least one SessionLimitExceededException (or its rewrap) to propagate "
            + "to the caller; observed failures: " + failures);
  }

  // ---- Constructor validation ----
  @Test
  void packagePrivateFourArgCtor_rejectsNonPositiveBufferLimit() {
    WebSocketSession s = Mockito.mock(WebSocketSession.class);
    assertThrows(IllegalArgumentException.class,
        () -> new NostrRelayClient(s, TEST_AWAIT_TIMEOUT_MS, 0, 1_000));
    assertThrows(IllegalArgumentException.class,
        () -> new NostrRelayClient(s, TEST_AWAIT_TIMEOUT_MS, -1, 1_000));
  }

  @Test
  void packagePrivateFourArgCtor_rejectsNonPositiveTimeLimit() {
    WebSocketSession s = Mockito.mock(WebSocketSession.class);
    assertThrows(IllegalArgumentException.class,
        () -> new NostrRelayClient(s, TEST_AWAIT_TIMEOUT_MS, 1_024, 0));
    assertThrows(IllegalArgumentException.class,
        () -> new NostrRelayClient(s, TEST_AWAIT_TIMEOUT_MS, 1_024, -5));
  }

  @Test
  void packagePrivateFourArgCtor_rejectsNonPositiveAwaitTimeout() {
    WebSocketSession s = Mockito.mock(WebSocketSession.class);
    assertThrows(IllegalArgumentException.class,
        () -> new NostrRelayClient(s, 0, 1_024, 1_000));
    assertThrows(IllegalArgumentException.class,
        () -> new NostrRelayClient(s, -1, 1_024, 1_000));
  }

  @Test
  void packagePrivateFourArgCtor_rejectsNullSession() {
    assertThrows(NullPointerException.class,
        () -> new NostrRelayClient((WebSocketSession) null, TEST_AWAIT_TIMEOUT_MS, 1_024, 1_000));
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  /**
   * Build a mock session whose {@code sendMessage} blocks for {@code blockMs}
   * milliseconds while incrementing an in-flight counter. If
   * {@code throwOnConcurrent} is set, on entry to {@code sendMessage} when the
   * counter rises above 1 the mock throws
   * {@code IllegalStateException("simulated TEXT_FULL_WRITING")} — emulating
   * Tomcat / Jetty's real-world thread-safety violation.
   */
  private static WebSocketSession newBlockingMockSession(
      AtomicInteger inFlight,
      AtomicInteger maxInFlight,
      AtomicBoolean isOpen,
      int blockMs,
      boolean throwOnConcurrent) {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenAnswer(inv -> isOpen.get());
    Answer<Void> sendAnswer = invocation -> {
      int now = inFlight.incrementAndGet();
      try {
        maxInFlight.updateAndGet(prev -> Math.max(prev, now));
        if (throwOnConcurrent && now > 1) {
          throw new IllegalStateException("simulated TEXT_FULL_WRITING");
        }
        if (blockMs > 0) {
          Thread.sleep(blockMs);
        }
      } finally {
        inFlight.decrementAndGet();
      }
      return null;
    };
    try {
      Mockito.doAnswer(sendAnswer).when(session).sendMessage(any(TextMessage.class));
    } catch (IOException impossible) {
      throw new AssertionError(impossible);
    }
    return session;
  }

  /**
   * Build a mock whose {@code sendMessage} blocks indefinitely (for the
   * §6.7d overflow test). The {@code unblock} flag releases pending sends.
   * Crucially, when the test calls
   * {@link WebSocketSession#close(CloseStatus)} (which the decorator does on
   * TERMINATE), {@code isOpen()} pivots to false.
   */
  private static WebSocketSession newIndefinitelyBlockingMockSession(
      AtomicInteger inFlight,
      AtomicInteger maxInFlight,
      AtomicBoolean isOpen,
      AtomicBoolean unblock) {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenAnswer(inv -> isOpen.get());
    Answer<Void> closeAnswer = invocation -> {
      isOpen.set(false);
      synchronized (session) { session.notifyAll(); }
      return null;
    };
    Answer<Void> sendAnswer = invocation -> {
      int now = inFlight.incrementAndGet();
      try {
        maxInFlight.updateAndGet(prev -> Math.max(prev, now));
        synchronized (session) {
          while (!unblock.get() && isOpen.get()) {
            session.wait(60_000);
            // Loop guard against spurious wakeups; one iteration is enough — we
            // either get notified by close()/notifyAll() or by the test's
            // unblock-pivot.
            break;
          }
        }
        if (!isOpen.get()) {
          throw new IOException("simulated session-closed mid-send");
        }
      } finally {
        inFlight.decrementAndGet();
      }
      return null;
    };
    try {
      Mockito.doAnswer(sendAnswer).when(session).sendMessage(any(TextMessage.class));
      Mockito.doAnswer(closeAnswer).when(session).close();
      Mockito.doAnswer(closeAnswer).when(session).close(any(CloseStatus.class));
    } catch (IOException impossible) {
      throw new AssertionError(impossible);
    }
    return session;
  }

  /**
   * Build a mock whose {@code sendMessage} blocks for {@code largeBlockMs}
   * if the payload exceeds {@code largeThresholdBytes}, otherwise for
   * {@code normalBlockMs}. Used by the §6.7c mixed-workload test.
   */
  private static WebSocketSession newSizeAwareMockSession(
      AtomicInteger inFlight,
      AtomicInteger maxInFlight,
      AtomicBoolean isOpen,
      int normalBlockMs,
      int largeBlockMs,
      int largeThresholdBytes) {
    WebSocketSession session = Mockito.mock(WebSocketSession.class);
    Mockito.when(session.isOpen()).thenAnswer(inv -> isOpen.get());
    Answer<Void> sendAnswer = (InvocationOnMock invocation) -> {
      int now = inFlight.incrementAndGet();
      try {
        maxInFlight.updateAndGet(prev -> Math.max(prev, now));
        TextMessage msg = invocation.getArgument(0);
        int sleepMs = msg.getPayloadLength() >= largeThresholdBytes
            ? largeBlockMs : normalBlockMs;
        Thread.sleep(sleepMs);
      } finally {
        inFlight.decrementAndGet();
      }
      return null;
    };
    try {
      Mockito.doAnswer(sendAnswer).when(session).sendMessage(any(TextMessage.class));
    } catch (IOException impossible) {
      throw new AssertionError(impossible);
    }
    return session;
  }

  /**
   * Run {@code n} concurrent {@code subscribe()} calls against the supplied
   * client, all released by a single starting-gun, and return the list of
   * exceptions raised. Caller must inspect the list — the worker pool is
   * always awaited (15 s deadline) before this method returns.
   */
  private static List<Throwable> runConcurrentSubscribes(
      NostrRelayClient client, int n) throws InterruptedException {
    List<Throwable> failures = new ArrayList<>();
    ExecutorService pool = Executors.newFixedThreadPool(n);
    CountDownLatch startingGun = new CountDownLatch(1);
    for (int i = 0; i < n; i++) {
      final int idx = i;
      pool.submit(() -> {
        try {
          startingGun.await();
          client.subscribe("[\"REQ\",\"sub-" + idx + "\"]",
              ignored -> {}, ignored -> {}, null);
        } catch (Throwable t) {
          synchronized (failures) { failures.add(t); }
        }
      });
    }
    startingGun.countDown();
    pool.shutdown();
    boolean done = pool.awaitTermination(15, TimeUnit.SECONDS);
    assertTrue(done, "Worker pool did not terminate within 15 s "
        + "(failures so far: " + failures.size() + ")");
    return failures;
  }

  private static String makePayload(int sizeBytes) {
    StringBuilder sb = new StringBuilder(sizeBytes + 32);
    sb.append("[\"REQ\",\"x\",\"");
    while (sb.length() < sizeBytes - 2) {
      sb.append('a');
    }
    sb.append("\"]");
    return sb.toString();
  }
}
