package nostr.api.unit;

import nostr.api.NIP65;
import nostr.base.Marker;
import nostr.base.Relay;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NIP65Test {

  @Test
  public void testCreateRelayListMetadataEvent() {
    Identity sender = Identity.generateRandomIdentity();
    NIP65 nip65 = new NIP65(sender);
    Relay relay = new Relay("wss://relay");
    nip65.createRelayListMetadataEvent(List.of(relay), Marker.READ);
    GenericEvent event = nip65.getEvent();
    assertEquals("r", event.getTags().get(0).getCode());
    assertTrue(event.getTags().get(0).toString().toUpperCase().contains(Marker.READ.name()));
  }

  @Test
  public void testCreateRelayListMetadataEventMapVariant() {
    Identity sender = Identity.generateRandomIdentity();
    NIP65 nip65 = new NIP65(sender);
    Relay r1 = new Relay("wss://relay1");
    Relay r2 = new Relay("wss://relay2");
    nip65.createRelayListMetadataEvent(Map.of(r1, Marker.READ, r2, Marker.WRITE));
    GenericEvent event = nip65.getEvent();
    assertEquals(nostr.base.Kind.RELAY_LIST_METADATA.getValue(), event.getKind());
    assertTrue(event.getTags().stream().anyMatch(t -> t.toString().contains("relay1")));
    assertTrue(event.getTags().stream().anyMatch(t -> t.toString().toUpperCase().contains(Marker.WRITE.name())));
  }

  @Test
  public void testRelayTagOrderPreserved() {
    Identity sender = Identity.generateRandomIdentity();
    NIP65 nip65 = new NIP65(sender);
    Relay r1 = new Relay("wss://r1");
    Relay r2 = new Relay("wss://r2");
    nip65.createRelayListMetadataEvent(List.of(r1, r2));
    GenericEvent event = nip65.getEvent();
    String t0 = event.getTags().get(0).toString();
    String t1 = event.getTags().get(1).toString();
    assertTrue(t0.contains("wss://r1"));
    assertTrue(t1.contains("wss://r2"));
  }
}
