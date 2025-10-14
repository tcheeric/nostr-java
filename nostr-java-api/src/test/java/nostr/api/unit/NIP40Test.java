package nostr.api.unit;

import nostr.api.NIP40;
import nostr.event.BaseTag;
import nostr.event.tag.ExpirationTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NIP40Test {

  @Test
  public void testCreateExpirationTag() {
    BaseTag tag = NIP40.createExpirationTag(10);
    assertEquals("expiration", tag.getCode());
    assertEquals(10, ((ExpirationTag) tag).getExpiration().intValue());
  }
}
