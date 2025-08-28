package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import nostr.api.NostrSpringWebSocketClient;
import nostr.api.service.NoteService;
import nostr.base.ISignable;
import nostr.base.Signature;
import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.id.SigningException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

  @Test
  // Verifies that SigningException bubbles up from the client when signing fails
  void signPropagatesSigningException() {
    String invalidPriv = "0000000000000000000000000000000000000000000000000000000000000000";
    Identity identity = Identity.create(invalidPriv);

    ISignable signable =
        new ISignable() {
          private Signature signature;

          @Override
          public Signature getSignature() {
            return signature;
          }

          @Override
          public void setSignature(Signature signature) {
            this.signature = signature;
          }

          @Override
          public Consumer<Signature> getSignatureConsumer() {
            return this::setSignature;
          }

          @Override
          public Supplier<ByteBuffer> getByteArraySupplier() {
            return () -> ByteBuffer.wrap("msg".getBytes(StandardCharsets.UTF_8));
          }
        };

    NoteService service = Mockito.mock(NoteService.class);
    NostrSpringWebSocketClient client = new NostrSpringWebSocketClient(service);

    assertThrows(SigningException.class, () -> client.sign(identity, signable));
  }
}
