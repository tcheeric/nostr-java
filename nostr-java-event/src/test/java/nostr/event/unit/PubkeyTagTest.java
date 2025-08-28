package nostr.event.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import nostr.base.PublicKey;
import nostr.event.tag.PubKeyTag;
import org.junit.jupiter.api.Test;

class PubkeyTagTest {

  @Test
  void getSupportedFields() {
    String sha256 = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
    PubKeyTag pubKeyTag = new PubKeyTag(new PublicKey(sha256));
    assertDoesNotThrow(
        () -> {
          Field field = pubKeyTag.getSupportedFields().stream().findFirst().orElseThrow();
          assertEquals("nostr.base.PublicKey", field.getAnnotatedType().toString());
          assertEquals("publicKey", field.getName());
          assertEquals(sha256, pubKeyTag.getFieldValue(field).orElseThrow());
        });
  }
}
