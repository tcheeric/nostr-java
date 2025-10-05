package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nostr.api.NIP14;
import nostr.event.BaseTag;
import nostr.event.tag.SubjectTag;
import org.junit.jupiter.api.Test;

public class NIP14Test {

  @Test
  public void testCreateSubjectTag() {
    BaseTag tag = NIP14.createSubjectTag("subj");
    assertEquals("subject", tag.getCode());
    assertEquals("subj", ((SubjectTag) tag).getSubject());
  }
}
