package nostr.api.unit;

import nostr.api.NostrSpringWebSocketClient;
import nostr.api.service.NoteService;
import org.mockito.Mockito;
import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NostrSpringWebSocketClientEventVerificationTest {

    @Test
    void sendEventThrowsWhenUnsigned() {
        GenericEvent event = new GenericEvent();
        event.setPubKey(Identity.generateRandomIdentity().getPublicKey());
        event.setKind(Constants.Kind.SHORT_TEXT_NOTE);
        event.setContent("test");

        NoteService service = Mockito.mock(NoteService.class);
        Mockito.when(service.send(Mockito.any(), Mockito.any())).thenReturn(List.of());
        NostrSpringWebSocketClient client = new NostrSpringWebSocketClient(service);
        assertThrows(IllegalStateException.class, () -> client.sendEvent(event));
    }

    @Test
    void sendEventReturnsEmptyListWhenSigned() {
        Identity identity = Identity.generateRandomIdentity();
        GenericEvent event = new GenericEvent(identity.getPublicKey(), Constants.Kind.SHORT_TEXT_NOTE);
        event.setContent("signed");
        identity.sign(event);

        NoteService service = Mockito.mock(NoteService.class);
        Mockito.when(service.send(Mockito.any(), Mockito.any())).thenReturn(List.of());
        NostrSpringWebSocketClient client = new NostrSpringWebSocketClient(service);
        List<String> responses = client.sendEvent(event);
        assertTrue(responses.isEmpty());
    }
}
