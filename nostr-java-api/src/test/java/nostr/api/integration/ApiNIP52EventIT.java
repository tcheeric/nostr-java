package nostr.api.integration;

import nostr.api.NIP52;
import nostr.api.util.JsonComparator;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class ApiNIP52EventIT extends BaseRelayIntegrationTest {
  private SpringWebSocketClient springWebSocketClient;

  @BeforeEach
    void setup() {
      springWebSocketClient = new SpringWebSocketClient(new StandardWebSocketClient(getRelayUri()), getRelayUri());
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

    var nip52 = new NIP52(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event = nip52.createCalendarTimeBasedEvent(tags, "content", createCalendarContent()).sign().getEvent();
    EventMessage message = new EventMessage(event);

    var expectedJson = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId()));
    var actualJson = MAPPER_AFTERBURNER.readTree(springWebSocketClient.send(message).stream().findFirst().orElseThrow());

    // Compare only first 3 elements of the JSON arrays
    assertTrue(
        JsonComparator.isEquivalentJson(
            MAPPER_AFTERBURNER.createArrayNode()
                .add(expectedJson.get(0)) // OK Command
                .add(expectedJson.get(1)) // event id
                .add(expectedJson.get(2)), // Accepted?
            MAPPER_AFTERBURNER.createArrayNode()
                .add(actualJson.get(0))
                .add(actualJson.get(1))
                .add(actualJson.get(2))));

    springWebSocketClient.closeSocket();
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }

  private CalendarContent<BaseTag> createCalendarContent() {
    return new CalendarContent<>(
        new IdentifierTag("UUID-CalendarTimeBasedEventTest"),
        "Calendar Time-Based Event title",
        1716513986268L);
  }
}
