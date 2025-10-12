package nostr.api.unit;

import nostr.api.NIP01;
import nostr.api.NIP03;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NIP03Test {

  @Test
  public void testCreateOtsEvent() {
    Identity sender = Identity.generateRandomIdentity();
    NIP01 nip01 = new NIP01(sender);
    GenericEvent ref = nip01.createTextNoteEvent("test").sign().getEvent();

    NIP03 nip03 = new NIP03(sender);
    nip03.createOtsEvent(ref, "ots", "alt");
    GenericEvent event = nip03.getEvent();

    assertNotNull(event);
    assertEquals(1040, event.getKind()); // Constants.Kind.OTS_ATTESTATION
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals("alt")));
    assertTrue(event.getTags().stream().anyMatch(t -> t instanceof EventTag));
  }
}
