
package nostr.types;

/**
 *
 * @author squirrel
 */
public interface IMarshaller {
    
    public abstract String marshall() throws MarshallException;    
}
