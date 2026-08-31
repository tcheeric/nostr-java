package nostr.mcp.subscription;

import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the buffer holds what arrived between polls, and admits what it lost. */
class EventBufferTest {

  // Verifies reading empties the buffer, so polling repeatedly does not refill an agent's
  // context with events it has already seen.
  @Test
  void drainingReturnsEachEventOnce() {
    EventBuffer buffer = new EventBuffer(10);
    buffer.add(note("first"));
    buffer.add(note("second"));

    assertEquals(2, buffer.drain().size());
    assertEquals(List.of(), buffer.drain());
  }

  // Verifies events come back in arrival order, since a feed read out of order reads as a
  // different conversation.
  @Test
  void eventsKeepTheirArrivalOrder() {
    EventBuffer buffer = new EventBuffer(10);
    buffer.add(note("first"));
    buffer.add(note("second"));
    buffer.add(note("third"));

    assertEquals(
        List.of("first", "second", "third"),
        buffer.drain().stream().map(GenericEvent::getContent).toList());
  }

  // Verifies a full buffer drops the oldest rather than refusing the newest, since a live feed
  // is more useful than a stalled one.
  @Test
  void afullBufferDropsTheOldest() {
    EventBuffer buffer = new EventBuffer(2);
    buffer.add(note("oldest"));
    buffer.add(note("middle"));
    buffer.add(note("newest"));

    assertEquals(
        List.of("middle", "newest"),
        buffer.drain().stream().map(GenericEvent::getContent).toList());
  }

  // Verifies dropped events are counted, because an agent told nothing about a gap will
  // summarise a partial feed as though it were the whole one.
  @Test
  void droppedEventsAreCounted() {
    EventBuffer buffer = new EventBuffer(2);
    buffer.add(note("one"));
    buffer.add(note("two"));
    buffer.add(note("three"));
    buffer.add(note("four"));

    assertEquals(2, buffer.droppedCount());
  }

  // Verifies the drop count survives draining, so an agent polling repeatedly can still learn
  // that a gap happened rather than only seeing it in the window it occurred.
  @Test
  void theDropCountSurvivesDraining() {
    EventBuffer buffer = new EventBuffer(1);
    buffer.add(note("one"));
    buffer.add(note("two"));
    buffer.drain();

    assertEquals(1, buffer.droppedCount());
  }

  // Verifies a repeated event is held once, since the SDK de-duplicates over a bounded window
  // and a relay replaying an old event later would otherwise look like a second post.
  @Test
  void aRepeatedEventIsHeldOnce() {
    EventBuffer buffer = new EventBuffer(10);
    GenericEvent event = note("same");
    buffer.add(event);
    buffer.add(event);

    assertEquals(1, buffer.depth());
  }

  // Verifies an event repeated after a drain is accepted again, since the agent has already been
  // given the first copy and dropping the second would silently lose a genuine re-delivery.
  @Test
  void anEventRepeatedAfterADrainIsAcceptedAgain() {
    EventBuffer buffer = new EventBuffer(10);
    GenericEvent event = note("same");
    buffer.add(event);
    buffer.drain();
    buffer.add(event);

    assertEquals(1, buffer.depth());
  }

  // Verifies depth reflects what is waiting, which is what tells an agent whether to read.
  @Test
  void depthReflectsWhatIsWaiting() {
    EventBuffer buffer = new EventBuffer(10);
    assertEquals(0, buffer.depth());

    buffer.add(note("one"));
    assertEquals(1, buffer.depth());

    buffer.drain();
    assertEquals(0, buffer.depth());
  }

  // Verifies concurrent arrivals are all recorded, since events reach the buffer on whichever
  // thread the transport dispatches them from.
  @Test
  void concurrentArrivalsAreAllRecorded() throws Exception {
    EventBuffer buffer = new EventBuffer(1000);
    List<Thread> threads =
        java.util.stream.IntStream.range(0, 10)
            .mapToObj(
                worker ->
                    Thread.ofVirtual()
                        .unstarted(
                            () -> {
                              for (int index = 0; index < 50; index++) {
                                buffer.add(note("worker " + worker + " event " + index));
                              }
                            }))
            .toList();
    threads.forEach(Thread::start);
    for (Thread thread : threads) {
      thread.join();
    }

    assertEquals(500, buffer.depth());
    assertTrue(buffer.droppedCount() == 0, "nothing should have been dropped under the capacity");
  }

  private GenericEvent note(String content) {
    Identity author = Identity.generateRandomIdentity();
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(author.getPublicKey())
            .kind(1)
            .content(content)
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    event.update();
    author.sign(event);
    return event;
  }
}
