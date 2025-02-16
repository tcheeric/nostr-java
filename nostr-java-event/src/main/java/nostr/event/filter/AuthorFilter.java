package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

@EqualsAndHashCode
public class AuthorFilter<T extends PublicKey> implements Filterable {
  public final static String filterKey = "authors";
  private final T publicKey;

  public AuthorFilter(T publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        this.publicKey.toHexString().equals(genericEvent.getPubKey().toHexString());
  }

  @Override
  public T getFilterCriterion() {
    return publicKey;
  }

  @Override
  public ArrayNode toArrayNode() {
    return mapper.createArrayNode().add(publicKey.toHexString());
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
