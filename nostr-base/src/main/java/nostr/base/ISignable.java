
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface ISignable {
    
    public abstract Signature getSignature();
    
    public abstract void setSignature(Signature signature);
}
