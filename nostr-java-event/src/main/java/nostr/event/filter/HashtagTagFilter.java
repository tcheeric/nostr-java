package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.HashtagTag;

@EqualsAndHashCode(callSuper = true)
public class HashtagTagFilter<T extends HashtagTag> extends AbstractFilterable<T> {
  public static final String FILTER_KEY = "#t";

  public HashtagTagFilter(T hashtagTag) {
    super(hashtagTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        Filterable.getTypeSpecificTags(HashtagTag.class, genericEvent).stream()
            .anyMatch(hashtagTag -> hashtagTag.getHashTag().equals(getFilterableValue()));
  }

  @Override
  public String getFilterableValue() {
    return getHashtagTag().getHashTag();
  }

  private T getHashtagTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn =
      node -> new HashtagTagFilter<>(new HashtagTag(node.asText()));
}
