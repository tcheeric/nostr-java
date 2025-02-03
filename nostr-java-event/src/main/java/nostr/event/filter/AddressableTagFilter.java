package nostr.event.filter;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;

import java.util.List;
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
    IdentifierTag addressTagIdentifierTag = this.addressableTag.getIdentifierTag();
    String addressTagPublicKey = this.addressableTag.getPublicKey().toHexString();
    Integer addressTagKind = this.addressableTag.getKind();

    String genericEventPubKey = genericEvent.getPubKey().toHexString();
    Integer genericEventKind = genericEvent.getKind();
    List<IdentifierTag> genericEventIdentifierTags = getIdentifierTags(genericEvent);

    return genericEventPubKey.equals(addressTagPublicKey) &&
        genericEventKind.equals(addressTagKind) &&
        genericEventIdentifierTags.stream().anyMatch(identifierTag ->
            identifierTag.getId().equals(addressTagIdentifierTag.getId()));
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
