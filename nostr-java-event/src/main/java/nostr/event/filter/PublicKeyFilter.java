package nostr.event.filter;

import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class PublicKeyFilter<T extends PublicKey> implements Filterable {
  private final T publicKey;

  public PublicKeyFilter(T publicKey) {
    this.publicKey = publicKey;
  }
  @Override
  public BiPredicate<T, GenericEvent> getBiPredicate() {
    return (publicKey, genericEvent) -> publicKey.toString().equals(genericEvent.getPubKey().toString());
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
}
