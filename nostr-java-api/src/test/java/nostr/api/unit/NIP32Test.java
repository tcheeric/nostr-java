package nostr.api.unit;

import nostr.api.NIP32;
import nostr.event.BaseTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NIP32Test {

  @Test
  public void testCreateTags() {
    BaseTag ns = NIP32.createNameSpaceTag("ns");
    assertEquals("L", ns.getCode());
    assertEquals("ns", ((LabelNamespaceTag) ns).getNameSpace());

    BaseTag label = NIP32.createLabelTag("label", "ns");
    assertEquals("l", label.getCode());
    assertEquals("label", ((LabelTag) label).getLabel());
    assertEquals("ns", ((LabelTag) label).getNameSpace());
  }
}
