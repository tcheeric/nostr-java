package nostr.event.filter;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;

import java.util.function.Function;
import java.util.function.Predicate;

public class AddressableTagFilter<T extends AddressTag> implements Filterable {
  public final static String filterKey = "#a";
  private final T addressableTag;

  public AddressableTagFilter(T addressableTag) {
    this.addressableTag = addressableTag;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return this::compare;
  }

  private boolean compare(@NonNull GenericEvent genericEvent) {
    return
        !genericEvent.getPubKey().toHexString().equals(
            this.addressableTag.getPublicKey().toHexString()) ||
        !genericEvent.getKind().equals(
            this.addressableTag.getKind()) ||
        getTypeSpecificTags(IdentifierTag.class, genericEvent).stream()
            .anyMatch(identifierTag ->
                identifierTag.getId().equals(
                    this.addressableTag.getIdentifierTag().getId()));
  }

  @Override
  public T getFilterCriterion() {
    return addressableTag;
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
