package nostr.event.filter;

import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.function.Predicate;

public class PublicKeyFilter<T extends PublicKey> implements Filterable {
  public final static String filterKey = "authors";
  private final T publicKey;

  public PublicKeyFilter(T publicKey) {
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
  public String toJson() {
    return publicKey.toHexString();
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
