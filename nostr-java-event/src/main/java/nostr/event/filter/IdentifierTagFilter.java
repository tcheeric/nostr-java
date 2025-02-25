package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.IdentifierTag;

import java.util.function.Function;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class IdentifierTagFilter<T extends IdentifierTag> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "#d";
  public IdentifierTagFilter(T identifierTag) {
    super(identifierTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(IdentifierTag.class, genericEvent).stream()
            .anyMatch(genericEventIdentifierTag ->
                genericEventIdentifierTag.getId().equals(getFilterableValue()));
  }

  @Override
  public String getFilterableValue() {
    return getIdentifierTag().getId();
  }

  private T getIdentifierTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn = node -> new IdentifierTagFilter<>(new IdentifierTag(node.asText()));
}
