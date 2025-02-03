package nostr.event.filter;

import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.function.Function;
import java.util.function.Predicate;

public class ReferencedPublicKeyFilter<T extends PublicKey> implements Filterable {
  public final static String filterKey = "#p";
  private final T publicKey;

  public ReferencedPublicKeyFilter(T publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getPubKey().toHexString().equals(this.publicKey.toHexString());
  }

  @Override
  public T getFilterCriterion() {
    return publicKey;
  }

  @Override
  public <T> Function<String, T> createContainedInstance() {
    return pubKeyString -> {
      PublicKey pubKey = new PublicKey(pubKeyString);
      return (T) pubKey;
    };
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
