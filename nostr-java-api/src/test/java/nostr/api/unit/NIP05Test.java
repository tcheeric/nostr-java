package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import nostr.api.NIP05;
import nostr.event.entities.UserProfile;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class NIP05Test {

  @Test
  public void testCreateInternetIdentifierMetadataEvent() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    NIP05 nip05 = new NIP05(sender);
    UserProfile profile =
        UserProfile.builder()
            .name("tester")
            .nip05("tester@example.com")
            .publicKey(sender.getPublicKey())
            .picture(URI.create("https://example.com/pic").toURL())
            .about("about")
            .build();

    nip05.createInternetIdentifierMetadataEvent(profile);
    GenericEvent event = nip05.getEvent();

    assertNotNull(event);
    assertEquals(0, event.getKind().intValue()); // Constants.Kind.USER_METADATA but 0
    assertTrue(event.getContent().contains("tester@example.com"));
  }
}
