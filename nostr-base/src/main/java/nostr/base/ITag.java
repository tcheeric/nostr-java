/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.base;

/**
 *
 * @author squirrel
 */
public interface ITag extends IElement {

    public abstract void setParent(IEvent event);
    
    public abstract String getCode();
}
