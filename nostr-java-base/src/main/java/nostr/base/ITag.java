
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface ITag extends IElement {

    void setParent(IEvent event);
    
    String getCode();
    
//    public abstract String printAttributes(Relay relay, boolean escape) throws NostrException;    
}
