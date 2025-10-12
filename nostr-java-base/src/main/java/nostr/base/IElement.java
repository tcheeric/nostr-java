package nostr.base;

/**
 * @author squirrel
 */
public interface IElement {

  default String getNip() {
    return "1";
  }
}
