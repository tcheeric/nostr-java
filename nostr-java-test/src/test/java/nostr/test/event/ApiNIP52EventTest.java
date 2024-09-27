package nostr.test.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import nostr.test.util.JsonComparator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiNIP52EventTest {
  private static final String RELAY_URI = "ws://localhost:5555";
  private final SpringWebSocketClient springWebSocketClient;

  public ApiNIP52EventTest() {
    springWebSocketClient = new SpringWebSocketClient(RELAY_URI);
  }

  @Test
  void testNIP52CalendarTimeBasedEventEventUsingSpringWebSocketClient() throws IOException {
    System.out.println("testNIP52CalendarTimeBasedEventEventUsingSpringWebSocketClient");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985"),
        null,
        "PAYER"));
    tags.add(new PubKeyTag(new PublicKey("494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347"),
        null,
        "PAYEE"));

    var nip52 = new NIP52<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event = nip52.createCalendarTimeBasedEvent(tags, "content", createCalendarContent()).sign().getEvent();
    EventMessage message = new EventMessage(event, event.getId());

    ObjectMapper mapper = new ObjectMapper();

    assertTrue(
        JsonComparator.isEquivalentJson(
            mapper.readTree(expectedResponseJson(event.getId())),
            mapper.readTree(springWebSocketClient.send(message).stream().findFirst().get())));

    springWebSocketClient.closeSocket();
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }

  private CalendarContent createCalendarContent() {
    return CalendarContent.builder(
        new IdentifierTag("UUID-CalendarTimeBasedEventTest"),
        "Calendar Time-Based Event title",
        1716513986268L).build();
  }
}
