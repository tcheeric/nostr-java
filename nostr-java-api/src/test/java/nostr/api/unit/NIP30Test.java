package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nostr.api.NIP30;
import nostr.event.BaseTag;
import nostr.event.tag.EmojiTag;
import org.junit.jupiter.api.Test;

public class NIP30Test {

  @Test
  public void testCreateEmojiTag() {
    BaseTag tag = NIP30.createEmojiTag("smile", "https://img");
    assertEquals("emoji", tag.getCode());
    assertEquals("smile", ((EmojiTag) tag).getShortcode());
    assertEquals("https://img", ((EmojiTag) tag).getUrl());
  }
}
