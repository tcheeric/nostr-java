package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;
import java.util.function.Predicate;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.UrlTag;

public class UrlTagFilter<T extends UrlTag> extends AbstractFilterable<T> {

  public static final String FILTER_KEY = "#u";

  public UrlTagFilter(T urlTag) {
    super(urlTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        Filterable.getTypeSpecificTags(UrlTag.class, genericEvent).stream()
            .anyMatch(urlTag -> urlTag.getUrl().equals(getFilterableValue()));
  }

  @Override
  public Object getFilterableValue() {
    return getUrlTag().getUrl();
  }

  private T getUrlTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn =
      node -> new UrlTagFilter<>(new UrlTag(node.asText()));
}
