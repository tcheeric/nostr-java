package nostr.event.filter;

import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GeohashTag;

import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class GeohashTagFilter<T extends GeohashTag> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "#g";

  public GeohashTagFilter(T geohashTag) {
    super(geohashTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        getTypeSpecificTags(GeohashTag.class, genericEvent).stream().anyMatch(geoHashTag ->
            geoHashTag.getLocation().equals(getFilterableValue()));
  }

  @Override
  public String getFilterableValue() {
    return getGeoHashTag().getLocation();
  }

  private T getGeoHashTag() {
    return super.getFilterable();
  }
}
