package nostr.event.filter;

import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;

import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class ReferencedPublicKeyFilter<T extends PubKeyTag> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "#p";

  public ReferencedPublicKeyFilter(T referencedPubKeyTag) {
    super(referencedPubKeyTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(PubKeyTag.class, genericEvent).stream()
            .anyMatch(pubKeyTag ->
                pubKeyTag.getPublicKey().toHexString().equals(getFilterableValue()));
  }

  @Override
  public String getFilterableValue() {
    return getReferencedPublicKey().getPublicKey().toHexString();
  }

  private T getReferencedPublicKey() {
    return super.getFilterable();
  }
}
