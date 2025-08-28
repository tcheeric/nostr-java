package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GeohashTag;

@EqualsAndHashCode(callSuper = true)
public class GeohashTagFilter<T extends GeohashTag> extends AbstractFilterable<T> {
  public static final String FILTER_KEY = "#g";

  public GeohashTagFilter(T geohashTag) {
    super(geohashTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        Filterable.getTypeSpecificTags(GeohashTag.class, genericEvent).stream()
            .anyMatch(geoHashTag -> geoHashTag.getLocation().equals(getFilterableValue()));
  }

  @Override
  public String getFilterableValue() {
    return getGeoHashTag().getLocation();
  }

  private T getGeoHashTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn =
      node -> new GeohashTagFilter<>(new GeohashTag(node.asText()));
}
