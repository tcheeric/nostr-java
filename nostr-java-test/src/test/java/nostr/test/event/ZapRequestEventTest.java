package nostr.test.event;

import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.ZapRequest;
import nostr.event.impl.ZapRequestEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.RelaysTag;
import nostr.event.tag.SubjectTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ZapRequestEventTest {
  public final PublicKey sender = Identity.generateRandomIdentity().getPublicKey();
  public final PubKeyTag recipient = new PubKeyTag(Identity.generateRandomIdentity().getPublicKey());

  public static final String PTAG_HEX = "2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985";
  public static final String ETAG_HEX = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347";
  public static final PubKeyTag P_TAG = new PubKeyTag(new PublicKey(PTAG_HEX));
  public static final EventTag E_TAG = new EventTag(ETAG_HEX);

  public static final String ZAP_REQUEST_CONTENT = "zap request content";
  public static final String SUBJECT = "Zap Request Subject";
  public static final Long AMOUNT = 1232456L;
  public static final String LNURL = "lnurl1dp68gurn8ghj7ar0wfsj6er9wchxuemjda4ju6t09ashq6f0w4ek2u30d3h82unv8a6xzeead3hkw6twye4nz0fcxgmnsef3vy6rsefkx93nyd338ycnvdeex9jxzcnzxeskvdekxq6rswr9x3nrqvfexvex2vf3vejnwvp4x3nr2wfhx56x2vmyv5mx2udztdn";
  public static final RelaysTag relaysTag = new RelaysTag(new Relay("ws://localhost:5555"));

  public static final SubjectTag SUBJECT_TAG = new SubjectTag(SUBJECT);
  public static final GeohashTag G_TAG = new GeohashTag("Zap Request Test Geohash Tag");
  public static final HashtagTag T_TAG = new HashtagTag("Zap Request Test Hashtag Tag");

  private ZapRequestEvent instance;

  @BeforeAll
  void setup() {
    List<BaseTag> tags = new ArrayList<>();
    tags.add(E_TAG);
    tags.add(P_TAG);
    tags.add(SUBJECT_TAG);
    tags.add(G_TAG);
    tags.add(T_TAG);
    tags.add(relaysTag);
    instance = new ZapRequestEvent(sender, recipient, tags, ZAP_REQUEST_CONTENT, new ZapRequest(relaysTag, AMOUNT, LNURL));
    instance.setSignature(Identity.generateRandomIdentity().sign(instance));
  }

  @Test
  void testConstructZapRequestEvent() {
    System.out.println("testConstructZapRequestEvent");

    Assertions.assertNotNull(instance.getTags());
    Assertions.assertNotNull(instance.getContent());
    Assertions.assertNotNull(instance.getZapRequest());

    Assertions.assertTrue(instance.getZapRequest().getRelaysTag().getRelays().stream().anyMatch(relay -> relay.getUri().equals(relaysTag.getRelays().stream().map(Relay::getUri).collect(Collectors.joining()))));
    Assertions.assertEquals(ZAP_REQUEST_CONTENT, instance.getContent());
    Assertions.assertEquals(LNURL, instance.getZapRequest().getLnUrl());
    Assertions.assertEquals(AMOUNT, instance.getZapRequest().getAmount());
  }
}