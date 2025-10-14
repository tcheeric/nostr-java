package nostr.api.unit;

import nostr.api.nip01.NIP01EventBuilder;
import nostr.base.PrivateKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NIP01EventBuilderTest {

  // Ensures that an explicitly provided sender overrides the default identity.
  @Test
  void buildTextNoteUsesOverrideIdentity() {
    Identity defaultSender = Identity.create(PrivateKey.generateRandomPrivKey());
    Identity overrideSender = Identity.create(PrivateKey.generateRandomPrivKey());
    NIP01EventBuilder builder = new NIP01EventBuilder(defaultSender);

    GenericEvent event = builder.buildTextNote(overrideSender, "override");

    assertEquals(overrideSender.getPublicKey(), event.getPubKey());
  }

  // Ensures that the builder falls back to the configured sender when no override is supplied.
  @Test
  void buildTextNoteUsesDefaultIdentityWhenOverrideMissing() {
    Identity defaultSender = Identity.create(PrivateKey.generateRandomPrivKey());
    NIP01EventBuilder builder = new NIP01EventBuilder(defaultSender);

    GenericEvent event = builder.buildTextNote("fallback");

    assertEquals(defaultSender.getPublicKey(), event.getPubKey());
  }
}
