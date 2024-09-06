package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.api.NIP15;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static nostr.test.event.ApiEventTest.createProduct;
import static nostr.test.event.ApiEventTest.createStall;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiEventUsingSpringWebSocketClientTest implements Subscriber<String> {
  private static final String RELAY_URI = "ws://localhost:5555";
  private final SpringWebSocketClient springWebSocketClient;
  private Subscription subscription;
  private String relayResponse = null;

  public ApiEventUsingSpringWebSocketClientTest() {
    springWebSocketClient = new SpringWebSocketClient(RELAY_URI);
  }

  @Test
  void testNIP15SendProductEventUsingSpringWebSocketClient() throws JsonProcessingException {
    System.out.println("testNIP15SendProductEventUsingSpringWebSocketClient");
    var product = createProduct(createStall());

    List<String> categories = new ArrayList<>();
    categories.add("bijoux");
    categories.add("Hommes");

    var nip15 = new NIP15<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event = nip15.createCreateOrUpdateProductEvent(product, categories).sign().getEvent();
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
}
