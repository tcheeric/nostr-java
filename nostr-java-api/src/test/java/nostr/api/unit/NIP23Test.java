package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import nostr.api.NIP23;
import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class NIP23Test {

  @Test
  public void testCreateLongFormTextNoteEvent() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    NIP23 nip23 = new NIP23(sender);
    nip23.creatLongFormTextNoteEvent("long");
    nip23.addTitleTag("title");
    nip23.addImageTag(new URL("https://example.com"));
    GenericEvent event = nip23.getEvent();

    assertEquals(Constants.Kind.LONG_FORM_TEXT_NOTE, event.getKind());
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals("title")));
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals("image")));
  }
}
