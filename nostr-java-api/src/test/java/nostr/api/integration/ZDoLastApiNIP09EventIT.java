package nostr.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;
import nostr.api.NIP01;
import nostr.api.NIP09;
import nostr.base.Kind;
import nostr.base.Relay;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.config.RelayConfig;
import nostr.event.BaseTag;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(RelayConfig.class)
@ActiveProfiles("test")
public class ZDoLastApiNIP09EventIT extends BaseRelayIntegrationTest {

  @Test
  public void deleteEvent() throws Exception {

    Identity identity = Identity.generateRandomIdentity();

    NIP09 nip09 = new NIP09(identity);
    NIP01 nip01 = new NIP01(identity);

    try (SpringWebSocketClient springWebSocketClient =
        new SpringWebSocketClient(new StandardWebSocketClient(getRelayUri()), getRelayUri())) {
      GenericEvent event = nip01.createTextNoteEvent("Delete me!").sign().getEvent();
      EventMessage message = new EventMessage(event);
      springWebSocketClient.send(message);

      Filters filters =
          new Filters(
              new KindFilter<>(Kind.TEXT_NOTE), new AuthorFilter<>(identity.getPublicKey()));

      List<String> result =
          NIP01.sendRequest(springWebSocketClient, filters, UUID.randomUUID().toString());

      assertFalse(result.isEmpty());
      assertEquals(2, result.size());

      var nip09Event = nip09.createDeletionEvent(nip01.getEvent()).sign().getEvent();
      EventMessage nip09Message = new EventMessage(nip09Event);
      springWebSocketClient.send(nip09Message);

      result = NIP01.sendRequest(springWebSocketClient, filters, UUID.randomUUID().toString());

      assertFalse(result.isEmpty());
      assertEquals(1, result.size());
    }

    nip01.close();
    nip09.close();
  }

  @Test
  public void deleteEventWithRef() throws Exception {
    final String RELAY_URI = getRelayUri();
    Identity identity = Identity.generateRandomIdentity();

    NIP01 nip011 = new NIP01(identity);
    GenericEvent replaceableEvent =
        nip011.createReplaceableEvent(10_001, "replaceable event").sign().getEvent();
    EventMessage replaceableEventMessage = new EventMessage(replaceableEvent);

    try (SpringWebSocketClient springWebSocketClient =
        new SpringWebSocketClient(new StandardWebSocketClient(getRelayUri()), getRelayUri())) {
      List<String> jsonReplaceableMessageList = springWebSocketClient.send(replaceableEventMessage);

      BaseMessageDecoder<OkMessage> decoder = new BaseMessageDecoder<>();
      OkMessage okMessage = decoder.decode(jsonReplaceableMessageList.get(0));

      assertNotNull(jsonReplaceableMessageList);
      assertInstanceOf(OkMessage.class, okMessage);

      IdentifierTag identifierTag = new IdentifierTag(replaceableEvent.getId());

      NIP01 nip01 = new NIP01(identity);
      nip01
          .createTextNoteEvent("Reference me!")
          .getEvent()
          .addTag(
              NIP01.createAddressTag(
                  10_001, identity.getPublicKey(), identifierTag, new Relay(RELAY_URI)));

      GenericEvent nip01Event = nip01.sign().getEvent();
      EventMessage eventMessage = new EventMessage(nip01Event);
      List<String> jsonMessageList = springWebSocketClient.send(eventMessage);

      decoder = new BaseMessageDecoder<>();
      okMessage = decoder.decode(jsonReplaceableMessageList.get(0));

      assertNotNull(jsonMessageList);
      assertInstanceOf(OkMessage.class, okMessage);

      NIP09 nip09 = new NIP09(identity);
      GenericEvent deletedEvent = nip09.createDeletionEvent(nip01Event).getEvent();

      assertEquals(4, deletedEvent.getTags().size());

      List<BaseTag> eventTags =
          deletedEvent.getTags().stream().filter(t -> "e".equals(t.getCode())).toList();

      assertEquals(1, eventTags.size());

      EventTag eventTag = (EventTag) eventTags.get(0);
      assertEquals(nip01Event.getId(), eventTag.getIdEvent());

      List<BaseTag> addressTags =
          deletedEvent.getTags().stream().filter(t -> "a".equals(t.getCode())).toList();

      assertEquals(1, addressTags.size());

      AddressTag addressTag = (AddressTag) addressTags.get(0);
      assertEquals(10_001, addressTag.getKind());
      assertEquals(replaceableEvent.getId(), addressTag.getIdentifierTag().getUuid());
      assertEquals(identity.getPublicKey(), addressTag.getPublicKey());

      List<BaseTag> kindTags =
          deletedEvent.getTags().stream().filter(t -> "k".equals(t.getCode())).toList();

      assertEquals(2, kindTags.size());

      nip01.close();
      nip011.close();
      nip09.close();
    }
  }
}
