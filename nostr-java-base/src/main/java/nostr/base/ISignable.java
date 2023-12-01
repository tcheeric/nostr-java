
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface ISignable {
    
    Signature getSignature();
    
    void setSignature(Signature signature);
}
