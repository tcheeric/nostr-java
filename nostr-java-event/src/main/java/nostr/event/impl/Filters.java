package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.event.Kind;
import nostr.event.filter.AddressableTagFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.FiltersCore;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.PublicKeyFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Filters {
  @JsonIgnore
  private final FiltersCore core;
  @Key
  private Integer limit;

  public Filters() {
    this.core = new FiltersCore();
  }

  public FiltersCore getFiltersCore() {
    return this.core;
  }

  @JsonProperty(EventFilter.filterKey)
  public List<GenericEvent> getEvents() {
    return getFilterableListByType(EventFilter.filterKey);
  }

  @JsonProperty(EventFilter.filterKey)
  public void setEvents(@NonNull List<GenericEvent> events) {
    setFilterableListByType(EventFilter.filterKey, events, EventFilter::new);
  }

  @JsonProperty(PublicKeyFilter.filterKey)
  public List<PublicKey> getAuthors() {
//    TODO: may possibly require below variant
//    getFilterableByType(ReferencedPublicKeyFilter.filterKey, PublicKeyFilter.class);
    return getFilterableListByType(PublicKeyFilter.filterKey);
  }

  @JsonProperty(PublicKeyFilter.filterKey)
  public void setAuthors(@NonNull List<PublicKey> authors) {
    setFilterableListByType(PublicKeyFilter.filterKey, authors, PublicKeyFilter::new);
  }

  @JsonProperty(KindFilter.filterKey)
  public List<Kind> getKinds() {
    return getFilterableListByType(KindFilter.filterKey);
  }

  @JsonProperty(KindFilter.filterKey)
  public void setKinds(@NonNull List<Kind> kinds) {
    setFilterableListByType(KindFilter.filterKey, kinds, KindFilter::new);
  }

  @JsonProperty(ReferencedEventFilter.filterKey)
  public List<GenericEvent> getReferencedEvents() {
    return getFilterableListByType(ReferencedEventFilter.filterKey);
  }

  @JsonProperty(ReferencedEventFilter.filterKey)
  public void setReferencedEvents(@NonNull List<GenericEvent> events) {
    setFilterableListByType(ReferencedEventFilter.filterKey, events, EventFilter::new);
  }

  @JsonProperty(ReferencedPublicKeyFilter.filterKey)
  public List<PublicKey> getReferencePubKeys() {
    return getFilterableListByType(ReferencedPublicKeyFilter.filterKey);
  }

  @JsonProperty(ReferencedPublicKeyFilter.filterKey)
  public void setReferencePubKeys(@NonNull List<PublicKey> publicKeys) {
    setFilterableListByType(ReferencedPublicKeyFilter.filterKey, publicKeys, ReferencedPublicKeyFilter::new);
  }

  @JsonProperty(SinceFilter.filterKey)
  public Optional<Long> getSince() {
    return getSinceOrUntil(SinceFilter.filterKey);
  }

  @JsonProperty(SinceFilter.filterKey)
  public void setSince(@NonNull Long since) {
    this.core.addFilterable(SinceFilter.filterKey, new SinceFilter(since));
  }

  @JsonProperty(UntilFilter.filterKey)
  public Optional<Long> getUntil() {
    return getSinceOrUntil(UntilFilter.filterKey);
  }

  @JsonProperty(UntilFilter.filterKey)
  public void setUntil(@NonNull Long until) {
    this.core.addFilterable(UntilFilter.filterKey, new UntilFilter(until));
  }

  @Setter(AccessLevel.NONE)
  private Map<String, List<String>> genericTagQuery;

  public List<GenericEvent> getReferenceforbelow() {
    return getFilterableListByType("asdfasdf");
  }

  @JsonAnyGetter
  public Map<String, List<String>> getGenericTagQuery() {
    this.genericTagQuery = Optional.ofNullable(genericTagQuery).orElse(new HashMap<>());
    return genericTagQuery;
  }

  public List<String> getGenericTagQuery(@NonNull String key) {
    return Optional.ofNullable(getGenericTagQuery().get(key)).orElse(Collections.emptyList());
  }

  //  TODO: add map content validation?
  @JsonAnySetter
  public void setGenericTagQuery(@NonNull Map<String, List<String>> map) {
    if (map.isEmpty())
      throw new IllegalArgumentException("generic tag query cannot be empty");
    map.forEach(this::setGenericTagQuery);
  }

  //  TODO: add map content validation?
  @JsonAnySetter
  public void setGenericTagQuery(@NonNull String key, @NonNull List<String> value) {
    if (key.isBlank())
      throw new IllegalArgumentException("key cannot be null");
    GenericTagQuery query = new GenericTagQuery();
    query.setTagName(key);
    query.setValue(value);
    setFilterableListByType(key, List.of(query), GenericTagQueryFilter::new);
    getGenericTagQuery().put(key, value);
  }

  @JsonProperty(AddressableTagFilter.filterKey)
  public List<AddressTag> getAddressableTags() {
    return getFilterableListByType(AddressableTagFilter.filterKey);
  }

  @JsonProperty(AddressableTagFilter.filterKey)
  public void setAddressableTags(@NonNull List<AddressTag> addressTags) {
    setFilterableListByType(AddressableTagFilter.filterKey, addressTags, AddressableTagFilter::new);
  }

  @JsonProperty(IdentifierTagFilter.filterKey)
  public List<IdentifierTag> getIdentifierTags() {
    return getFilterableListByType(IdentifierTagFilter.filterKey);
  }

  @JsonProperty(IdentifierTagFilter.filterKey)
  public void setIdentifierTags(@NonNull List<IdentifierTag> identifierTags) {
    setFilterableListByType(IdentifierTagFilter.filterKey, identifierTags, IdentifierTagFilter::new);
  }

  private <T> List<T> getFilterableListByType(@NonNull String type) {
    return Optional
        .ofNullable(
            this.core.getFilterableByType(type))
        .stream().flatMap(filterables ->
            filterables.stream().map(filterable ->
                (T) filterable.getFilterCriterion()))
        .toList();
  }

  @SneakyThrows
  private <T> void setFilterableListByType(
      @NonNull String key,
      @NonNull List<T> filterTypeList,
      @NonNull Function<T, Filterable> filterableFunction) {

    if (filterTypeList.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("[%s] filter must contain at least one element", key));
    }

    this.core.addFilterable(
        key,
        filterTypeList.stream().map(filterableFunction).collect(Collectors.toList()));
//        .orElseThrow(() ->
//            new IllegalArgumentException(
//                String.format("[%s] filter must contain at least one element")))
  }

  private Optional<Long> getSinceOrUntil(String type) {
    return Optional
        .ofNullable(
            this.core.getFilterableByType(type))
        .map(filterables -> filterables
            .getFirst().getFilterCriterion());
  }

  public Optional<Integer> getLimit() {
    return Optional.ofNullable(limit);
  }

  public void setLimit(@NonNull Integer limit) {
    this.limit = limit;
  }

  public static FiltersBuilder builder() {
    return new FiltersBuilder();
  }

  public static class FiltersBuilder {
    private final Filters filters = new Filters();

    @JsonProperty(EventFilter.filterKey)
    public FiltersBuilder events(@NonNull List<GenericEvent> events) {
      filters.setEvents(events);
      return this;
    }

    @JsonProperty(PublicKeyFilter.filterKey)
    public FiltersBuilder authors(@NonNull List<PublicKey> authors) {
      filters.setAuthors(authors);
      return this;
    }

    @JsonProperty(KindFilter.filterKey)
    public FiltersBuilder kinds(@NonNull List<Kind> kinds) {
      filters.setKinds(kinds);
      return this;
    }

    @JsonProperty(ReferencedEventFilter.filterKey)
    public FiltersBuilder referencedEvents(@NonNull List<GenericEvent> events) {
      filters.setReferencedEvents(events);
      return this;
    }

    @JsonProperty(ReferencedPublicKeyFilter.filterKey)
    public FiltersBuilder referencePubKeys(@NonNull List<PublicKey> publicKeys) {
      filters.setReferencePubKeys(publicKeys);
      return this;
    }

    @JsonProperty(SinceFilter.filterKey)
    public FiltersBuilder since(@NonNull Long since) {
      filters.setSince(since);
      return this;
    }

    @JsonProperty(UntilFilter.filterKey)
    public FiltersBuilder until(@NonNull Long until) {
      filters.setUntil(until);
      return this;
    }

    public FiltersBuilder genericTagQuery(@NonNull Map<String, List<String>> map) {
      filters.setGenericTagQuery(map);
      return this;
    }

    public FiltersBuilder limit(@NonNull Integer limit) {
      filters.setLimit(limit);
      return this;
    }

    public Filters build() {
      return filters;
    }
  }
}
