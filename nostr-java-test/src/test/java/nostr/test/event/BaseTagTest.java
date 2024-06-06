package nostr.test.event;

import lombok.SneakyThrows;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericTag;
import nostr.event.tag.PubKeyTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseTagTest {
  BaseTag genericTag = GenericTag.create("id", 1, "value");

  @Test
  void getNip() {
    assertEquals(1, genericTag.getNip());
  }

  @SneakyThrows
  @Test
  void getSupportedFields() {
    String sha256 = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
    PubKeyTag pubKeyTag = new PubKeyTag(new PublicKey(sha256));
    assertEquals(sha256, pubKeyTag.getFieldValue(pubKeyTag.getSupportedFields().stream().findFirst().orElseThrow()));
  }

  @Test
  void testHashCode() {
    assertEquals(112174237, genericTag.hashCode());
  }

  @Test
  void testToString() {
    String result = "GenericTag(code=id, nip=1, attributes=[ElementAttribute(name=param0, value=value, nip=null)])";
    assertEquals(result, genericTag.toString());
  }
}