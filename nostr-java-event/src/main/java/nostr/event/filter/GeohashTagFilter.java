package nostr.event.filter;

import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GeohashTag;

import java.util.function.Predicate;

@EqualsAndHashCode
public class GeohashTagFilter<T extends GeohashTag> implements Filterable {
  public final static String filterKey = "#g";
  private final T geohashTag;

  public GeohashTagFilter(T geohashTag) {
    this.geohashTag = geohashTag;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(GeohashTag.class, genericEvent).stream().anyMatch(geoHashTag ->
            geoHashTag.getLocation().equals(this.geohashTag.getLocation()));
  }

  @Override
  public T getFilterCriterion() {
    return geohashTag;
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  @Override
  public String getFilterableValue() {
    return geohashTag.getLocation();
  }
}
