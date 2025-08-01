package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.function.Function;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class AuthorFilter<T extends PublicKey> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "authors";

  public AuthorFilter(T publicKey) {
    super(publicKey, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        genericEvent.getPubKey().toHexString().equals(getFilterableValue());
  }

  @Override
  public String getFilterableValue() {
    return getAuthor().toHexString();
  }

  private T getAuthor() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn = node -> new AuthorFilter<>(new PublicKey(node.asText()));
}
