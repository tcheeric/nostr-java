
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface ITag extends IElement {

    public abstract void setParent(IEvent event);
    
    public abstract String getCode();
    
//    public abstract String printAttributes(Relay relay, boolean escape) throws NostrException;    
}
