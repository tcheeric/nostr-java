package nostr.base;

import java.util.List;

/**
 *
 * @author squirrel
 */
public interface IGenericElement extends IElement {

    List<ElementAttribute> getAttributes();

    void addAttribute(ElementAttribute attribute);
}
