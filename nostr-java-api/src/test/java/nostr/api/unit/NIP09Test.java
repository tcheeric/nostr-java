package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import nostr.api.NIP01;
import nostr.api.NIP09;
import nostr.base.Kind;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class NIP09Test {

  @Test
  public void testCreateDeletionEvent() {
    Identity sender = Identity.generateRandomIdentity();
    NIP01 nip01 = new NIP01(sender);
    GenericEvent note = nip01.createTextNoteEvent("del me").getEvent();

    NIP09 nip09 = new NIP09(sender);
    nip09.createDeletionEvent(List.of(note));
    GenericEvent event = nip09.getEvent();

    assertEquals(Kind.DELETION.getValue(), event.getKind());
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals("e")));
  }
}
