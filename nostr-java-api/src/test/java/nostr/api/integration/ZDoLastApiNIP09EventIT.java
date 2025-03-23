package nostr.api.integration;

import nostr.api.NIP01;
import nostr.api.NIP09;
import nostr.base.Relay;
import nostr.config.RelayConfig;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ReplaceableEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.message.OkMessage;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(RelayConfig.class)
@ActiveProfiles("test")
@Retry
public class ZDoLastApiNIP09EventIT {
    @Autowired
    private Map<String, String> relays;

    @Test
    public void deleteEvent() throws IOException {

        Identity identity = Identity.generateRandomIdentity();
        NIP09<?> nip09 = new NIP09<>(identity);

        NIP01<TextNoteEvent> nip01 = new NIP01<>(identity);
        nip01.createTextNoteEvent("Delete me!").signAndSend(relays);

        Filters filters = new Filters(
            new KindFilter<>(Kind.TEXT_NOTE),
            new AuthorFilter<>(identity.getPublicKey()));

        List<String> result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        nip09.createDeletionEvent(nip01.getEvent()).signAndSend(relays);

        result = nip01.sendRequest(filters, UUID.randomUUID().toString());

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

//        nip01.close();
//        nip09.close();
    }


    @Test
    public void deleteEventWithRef() throws IOException {
        final String RELAY_URI = "ws://localhost:5555";
        Identity identity = Identity.generateRandomIdentity();

        NIP01<ReplaceableEvent> nip011 = new NIP01<>(identity);
        BaseMessage replaceableMessage = nip011.createReplaceableEvent(10_001, "replaceable event").signAndSend(relays);

        assertNotNull(replaceableMessage);
        assertTrue(replaceableMessage instanceof OkMessage);

        GenericEvent replaceableEvent = nip011.getEvent();
        IdentifierTag identifierTag = new IdentifierTag(replaceableEvent.getId());

        NIP01<TextNoteEvent> nip01 = new NIP01<>(identity);
        nip01
            .createTextNoteEvent("Reference me!")
            .getEvent()
            .addTag(nip01.createAddressTag(10_001, identity.getPublicKey(), identifierTag, new Relay(RELAY_URI)));

        BaseMessage message = nip01.signAndSend(relays);

        assertNotNull(message);
        assertTrue(message instanceof OkMessage);

        GenericEvent event = nip01.getEvent();

        NIP09<?> nip09 = new NIP09<>(identity);
        GenericEvent deletedEvent = nip09.createDeletionEvent(event).getEvent();

        assertEquals(4, deletedEvent.getTags().size());

        List<BaseTag> eventTags = deletedEvent.getTags()
            .stream()
            .filter(t -> "e".equals(t.getCode()))
            .collect(Collectors.toList());

        assertEquals(1, eventTags.size());

        EventTag eventTag = (EventTag) eventTags.get(0);
        assertEquals(event.getId(), eventTag.getIdEvent());

        List<BaseTag> addressTags = deletedEvent.getTags()
            .stream()
            .filter(t -> "a".equals(t.getCode()))
            .collect(Collectors.toList());

        assertEquals(1, addressTags.size());

        AddressTag addressTag = (AddressTag) addressTags.get(0);
        assertEquals(10_001, addressTag.getKind());
        assertEquals(replaceableEvent.getId(), addressTag.getIdentifierTag().getId());
        assertEquals(identity.getPublicKey(), addressTag.getPublicKey());

        List<BaseTag> kindTags = deletedEvent.getTags()
            .stream()
            .filter(t -> "k".equals(t.getCode()))
            .collect(Collectors.toList());

        assertEquals(2, kindTags.size());

        nip09.signAndSend(relays);

//        nip01.close();
//        nip011.close();
//        nip09.close();
    }
}
