package nostr.api.unit;

import nostr.api.NIP28;
import nostr.base.Kind;
import nostr.event.entities.ChannelProfile;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NIP28Test {

  @Test
  public void testCreateChannelCreateEvent() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    NIP28 nip28 = new NIP28(sender);
    ChannelProfile profile =
        new ChannelProfile("channel", "about", new java.net.URL("https://example.com"));
    nip28.createChannelCreateEvent(profile);
    GenericEvent event = nip28.getEvent();

    assertEquals(Kind.CHANNEL_CREATE.getValue(), event.getKind());
    assertTrue(event.getContent().contains("channel"));
  }

  @Test
  public void testUpdateChannelMetadataEvent() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    NIP28 nip28 = new NIP28(sender);
    ChannelProfile profile =
        new ChannelProfile("channel", "about", new java.net.URL("https://example.com"));
    nip28.createChannelCreateEvent(profile);
    GenericEvent channelCreate = nip28.getEvent();

    ChannelProfile updated =
        new ChannelProfile("updated", "changed", new java.net.URL("https://example.com/2"));
    nip28.updateChannelMetadataEvent(channelCreate, updated, null);
    GenericEvent metadataEvent = nip28.getEvent();

    assertEquals(Kind.CHANNEL_METADATA.getValue(), metadataEvent.getKind());
    assertTrue(metadataEvent.getContent().contains("updated"));
    assertFalse(metadataEvent.getTags().isEmpty());
  }
}
