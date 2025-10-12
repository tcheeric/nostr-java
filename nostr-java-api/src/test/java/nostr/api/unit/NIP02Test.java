package nostr.api.unit;

import nostr.api.NIP02;
import nostr.base.Kind;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NIP02Test {

  private Identity sender;
  private NIP02 nip02;

  @BeforeEach
  void setUp() {
    sender = Identity.generateRandomIdentity();
    nip02 = new NIP02(sender);
  }

  @Test
  void testCreateContactListEvent() {
    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(sender.getPublicKey()));
    nip02.createContactListEvent(new ArrayList<>(tags));
    assertNotNull(nip02.getEvent(), "Event should be created");
    assertEquals(
        Kind.CONTACT_LIST.getValue(), nip02.getEvent().getKind(), "Kind should be CONTACT_LIST");
  }

  @Test
  void testAddContactTag() {
    BaseTag pTag = new PubKeyTag(sender.getPublicKey());
    nip02.createContactListEvent(new ArrayList<>());
    nip02.addContactTag(pTag);
    assertTrue(
        nip02.getEvent().getTags().stream()
            .anyMatch(t -> t.getCode().equals(Constants.Tag.PUBKEY_CODE)),
        "Contact tag should be added");
  }

  @Test
  void testAddContactTagWithPublicKey() {
    nip02.createContactListEvent(new ArrayList<>());
    nip02.addContactTag(sender.getPublicKey());
    assertTrue(
        nip02.getEvent().getTags().stream()
            .anyMatch(t -> t.getCode().equals(Constants.Tag.PUBKEY_CODE)),
        "Contact tag from public key should be added");
  }

  @Test
  void testAddContactTagThrowsException() {
    nip02.createContactListEvent(new ArrayList<>());
    BaseTag invalidTag = BaseTag.create("x", "invalid");
    assertThrows(
        IllegalArgumentException.class,
        () -> nip02.addContactTag(invalidTag),
        "Should throw if added tag is not a 'p' tag");
  }
}
