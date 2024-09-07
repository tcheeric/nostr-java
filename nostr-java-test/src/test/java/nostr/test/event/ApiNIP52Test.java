package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.api.NIP52;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiNIP52Test implements Subscriber<String> {
  private static final String RELAY_URI = "ws://localhost:5555";
  private final SpringWebSocketClient springWebSocketClient;
  private Subscription subscription;
  private String relayResponse = null;

  public ApiNIP52Test() {
    springWebSocketClient = new SpringWebSocketClient(RELAY_URI);
  }

  @Test
  void testNIP52CalendarTimeBasedEventEventUsingSpringWebSocketClient() throws JsonProcessingException {
    System.out.println("testNIP52CalendarTimeBasedEventEventUsingSpringWebSocketClient");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985"),
        null,
        "ISSUER"));
    tags.add(new PubKeyTag(new PublicKey("494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347"),
        null,
        "COUNTERPARTY"));

    var nip52 = new NIP52<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event = nip52.createCalendarTimeBasedEvent(tags, "content", createCalendarContent()).sign().getEvent();
    EventMessage message = new EventMessage(event, event.getId());

    springWebSocketClient.send(message).subscribeWith(this);
    await().until(() -> Objects.nonNull(relayResponse));
    assertEquals(expectedResponseJson(event.getId()), relayResponse);
    springWebSocketClient.closeSocket();
  }


  @Override
  public void onSubscribe(Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);
  }

  @Override
  public void onNext(String s) {
    subscription.request(1);
    relayResponse = s;
  }

  @Override
  public void onError(Throwable throwable) {
  }

  @Override
  public void onComplete() {
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }

  private CalendarContent createCalendarContent() {
//    return new CalendarContent(
//        new IdentifierTag("UUID-CalendarTimeBasedEventTest"),
//        "Calendar Time-Based Event title",
//        1716513986268L);
    return CalendarContent.builder(
        new IdentifierTag("UUID-CalendarTimeBasedEventTest"),
        "Calendar Time-Based Event title",
        1716513986268L).build();
  }
}
