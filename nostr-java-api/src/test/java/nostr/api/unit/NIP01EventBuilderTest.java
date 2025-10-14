package nostr.api.unit;

import nostr.api.nip01.NIP01EventBuilder;
import nostr.base.PrivateKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NIP01EventBuilderTest {

  // Ensures that updating the default sender identity is respected by the builder.
  @Test
  void buildTextNoteUsesUpdatedIdentity() {
    Identity defaultSender = Identity.create(PrivateKey.generateRandomPrivKey());
    Identity overrideSender = Identity.create(PrivateKey.generateRandomPrivKey());
    NIP01EventBuilder builder = new NIP01EventBuilder(defaultSender);

    // Update the default sender and ensure new events use it
    builder.updateDefaultSender(overrideSender);
    GenericEvent event = builder.buildTextNote("override");

    assertEquals(overrideSender.getPublicKey(), event.getPubKey());
  }

  // Ensures that the builder uses the initially configured default sender when no update occurs.
  @Test
  void buildTextNoteUsesDefaultIdentityWhenOverrideMissing() {
    Identity defaultSender = Identity.create(PrivateKey.generateRandomPrivKey());
    NIP01EventBuilder builder = new NIP01EventBuilder(defaultSender);

    GenericEvent event = builder.buildTextNote("fallback");

    assertEquals(defaultSender.getPublicKey(), event.getPubKey());
  }
}
