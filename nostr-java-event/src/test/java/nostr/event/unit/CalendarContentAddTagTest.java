package nostr.event.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import nostr.base.PublicKey;
import nostr.event.entities.CalendarContent;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

public class CalendarContentAddTagTest {

  @Test
  // Ensures adding two hashtag tags results in exactly two items without duplication.
  void testAddTwoHashtagTagsNoDuplication() {
    CalendarContent<?> content = new CalendarContent<>(new IdentifierTag("id-1"), "title", 1L);

    HashtagTag t1 = new HashtagTag("tag1");
    HashtagTag t2 = new HashtagTag("tag2");

    content.addHashtagTag(t1);
    content.addHashtagTag(t2);

    List<HashtagTag> tags = content.getHashtagTags();
    assertEquals(2, tags.size());
    assertEquals("tag1", tags.get(0).getHashTag());
    assertEquals("tag2", tags.get(1).getHashTag());
  }

  @Test
  // Verifies adding a participant PubKeyTag produces a single entry with the expected key.
  void testAddParticipantPubKeyTagNoDuplication() {
    CalendarContent<?> content = new CalendarContent<>(new IdentifierTag("id-2"), "title", 1L);

    String hex = "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798";
    PubKeyTag p = new PubKeyTag(new PublicKey(hex));
    content.addParticipantPubKeyTag(p);

    List<PubKeyTag> pTags = content.getParticipantPubKeyTags();
    assertEquals(1, pTags.size());
    assertEquals(hex, pTags.get(0).getPublicKey().toString());
  }

  @Test
  // Confirms different tag types are tracked independently with correct counts.
  void testAddMultipleTagTypesIndependent() {
    CalendarContent<?> content = new CalendarContent<>(new IdentifierTag("id-3"), "title", 1L);

    // Add two hashtags
    content.addHashtagTag(new HashtagTag("a"));
    content.addHashtagTag(new HashtagTag("b"));

    // Add one participant pubkey
    content.addParticipantPubKeyTag(
        new PubKeyTag(
            new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984")));

    assertEquals(2, content.getHashtagTags().size());
    assertEquals(1, content.getParticipantPubKeyTags().size());
    assertTrue(content.getGeohashTag().isEmpty());
  }
}

