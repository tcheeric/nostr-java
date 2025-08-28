package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import nostr.api.NIP12;
import nostr.event.BaseTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.ReferenceTag;
import org.junit.jupiter.api.Test;

public class NIP12Test {

  @Test
  public void testCreateTags() throws Exception {
    BaseTag hTag = NIP12.createHashtagTag("nostr");
    assertEquals("t", hTag.getCode());
    assertEquals("nostr", ((HashtagTag) hTag).getHashTag());

    URL url = new URL("https://example.com");
    BaseTag rTag = NIP12.createReferenceTag(url);
    assertEquals("r", rTag.getCode());
    assertEquals(url.toString(), ((ReferenceTag) rTag).getUri().toString());

    BaseTag gTag = NIP12.createGeohashTag("loc");
    assertEquals("g", gTag.getCode());
    assertEquals("loc", ((GeohashTag) gTag).getLocation());
  }
}
