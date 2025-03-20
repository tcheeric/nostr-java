package nostr.base;

import java.util.List;

public interface IGenericElement extends IElement {
    List<ElementAttribute> getAttributes();
    void addAttribute(ElementAttribute... attribute);
    void addAttributes(List<ElementAttribute> attributes);
}
