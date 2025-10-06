package nostr.event.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.Test;

class GenericEventBuilderTest {

  private static final String HEX_ID = "a3f2d7306f8911b588f7c5e2d460ad4f8b5e2c5d7a6b8c9d0e1f2a3b4c5d6e7f";
  private static final PublicKey PUBLIC_KEY =
      new PublicKey("f6f8a2d4c6e8b0a1f2d3c4b5a6e7d8c9b0a1c2d3e4f5a6b7c8d9e0f1a2b3c4d");

  // Ensures the builder populates core fields when provided with a standard Kind enum.
  @Test
  void shouldBuildGenericEventWithStandardKind() {
    BaseTag titleTag = BaseTag.create("title", "Builder test");

    GenericEvent event =
        GenericEvent.builder()
            .id(HEX_ID)
            .pubKey(PUBLIC_KEY)
            .kind(Kind.TEXT_NOTE)
            .tags(List.of(titleTag))
            .content("hello world")
            .createdAt(1_700_000_000L)
            .build();

    assertEquals(HEX_ID, event.getId());
    assertEquals(PUBLIC_KEY, event.getPubKey());
    assertEquals(Kind.TEXT_NOTE.getValue(), event.getKind());
    assertEquals("hello world", event.getContent());
    assertEquals(1_700_000_000L, event.getCreatedAt());
    assertEquals(1, event.getTags().size());
    assertEquals("title", event.getTags().get(0).getCode());
  }

  // Ensures custom kinds outside the enum can be provided through the builder's customKind field.
  @Test
  void shouldBuildGenericEventWithCustomKind() {
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(PUBLIC_KEY)
            .customKind(65_535)
            .tags(List.of())
            .content("")
            .createdAt(1L)
            .build();

    assertEquals(65_535, event.getKind());
  }

  // Ensures the builder fails fast when neither an enum nor custom kind is supplied.
  @Test
  void shouldRequireKindWhenBuilding() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            GenericEvent.builder()
                .pubKey(PUBLIC_KEY)
                .tags(List.of())
                .content("missing kind")
                .createdAt(2L)
                .build());
  }
}
