package nostr.api.unit;

import nostr.api.NIP14;
import nostr.event.BaseTag;
import nostr.event.tag.SubjectTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NIP14Test {

  @Test
  public void testCreateSubjectTag() {
    BaseTag tag = NIP14.createSubjectTag("subj");
    assertEquals("subject", tag.getCode());
    assertEquals("subj", ((SubjectTag) tag).getSubject());
  }
}
