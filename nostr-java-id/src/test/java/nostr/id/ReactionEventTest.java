package nostr.id;

import nostr.base.PublicKey;
import nostr.event.entities.Reaction;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ReactionEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReactionEventTest {

  @Test
  void testGetReactedEventId() {
    PublicKey pk = Identity.generateRandomIdentity().getPublicKey();
    GenericEvent original = EntityFactory.Events.createTextNoteEvent(pk);
    original.update();
    ReactionEvent reaction = EntityFactory.Events.createReactionEvent(pk, original);
    reaction.update();
    assertEquals(original.getId(), reaction.getReactedEventId());
  }

  @Test
  void testMissingEventTag() {
    PublicKey pk = Identity.generateRandomIdentity().getPublicKey();
    ReactionEvent reaction = new ReactionEvent(pk, new ArrayList<>(), Reaction.LIKE.getEmoji());
    assertThrows(AssertionError.class, reaction::getReactedEventId);
  }
}
