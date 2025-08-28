package nostr.base;

/**
 * @author squirrel
 */
public interface ITag extends IElement {

  void setParent(IEvent event);

  String getCode();
}
