package nostr.event.filter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;

import java.util.function.Predicate;

@EqualsAndHashCode
public class ReferencedPublicKeyFilter<T extends PublicKey> implements Filterable {
  public final static String filterKey = "#p";
  private final T referencedPublicKey;

  public ReferencedPublicKeyFilter(T referencedPublicKey) {
    this.referencedPublicKey = referencedPublicKey;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(PubKeyTag.class, genericEvent).stream()
            .anyMatch(pubKeyTag ->
                pubKeyTag.getPublicKey().toHexString().equals(this.referencedPublicKey.toHexString()));
  }

  @Override
  public T getFilterCriterion() {
    return referencedPublicKey;
  }

  @Override
  public ArrayNode toArrayNode() {
    return mapper.createArrayNode().add(referencedPublicKey.toHexString());
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }
}
