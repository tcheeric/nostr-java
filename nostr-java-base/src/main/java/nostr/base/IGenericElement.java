package nostr.base;

import java.util.Set;

/**
 *
 * @author squirrel
 */
public interface IGenericElement {

    public abstract Set<ElementAttribute> getAttributes();

    public abstract void addAttribute(ElementAttribute attribute);
}
