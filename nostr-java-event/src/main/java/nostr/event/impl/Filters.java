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
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.FiltersCore;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.PublicKeyFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;

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

  @JsonProperty("ids")
  public List<GenericEvent> getEvents() {
    return getFilterableListByType("ids");
  }

  @JsonProperty("ids")
  public void setEvents(@NonNull List<GenericEvent> events) {
    setFilterableListByType("ids", events, EventFilter::new);
  }

  @JsonProperty("authors")
  public List<PublicKey> getAuthors() {
//    TODO: may possibly require below variant
//    getFilterableByType("authors", PublicKeyFilter.class);
    return getFilterableListByType("authors");
  }

  @JsonProperty("authors")
  public void setAuthors(@NonNull List<PublicKey> authors) {
    setFilterableListByType("authors", authors, PublicKeyFilter::new);
  }

  @JsonProperty("kinds")
  public List<Kind> getKinds() {
    return getFilterableListByType("kinds");
  }

  @JsonProperty("kinds")
  public void setKinds(@NonNull List<Kind> kinds) {
    setFilterableListByType("kinds", kinds, KindFilter::new);
  }

  @JsonProperty("#e")
  public List<GenericEvent> getReferencedEvents() {
    return getFilterableListByType("#e");
  }

  @JsonProperty("#e")
  public void setReferencedEvents(@NonNull List<GenericEvent> events) {
    setFilterableListByType("#e", events, EventFilter::new);
  }

  @JsonProperty("#p")
  public List<PublicKey> getReferencePubKeys() {
    return getFilterableListByType("#p");
  }

  @JsonProperty("#p")
  public void setReferencePubKeys(@NonNull List<PublicKey> publicKeys) {
    setFilterableListByType("#p", publicKeys, ReferencedPublicKeyFilter::new);
  }

  @JsonProperty("since")
  public Optional<Long> getSince() {
    return getSinceOrUntil("since");
  }

  @JsonProperty("since")
  public void setSince(@NonNull Long since) {
    this.core.addFilter("since", new SinceFilter<>(since));
  }

  @JsonProperty("until")
  public Optional<Long> getUntil() {
    return getSinceOrUntil("until");
  }

  @JsonProperty("until")
  public void setUntil(@NonNull Long until) {
    this.core.addFilter("until", new UntilFilter<>(until));
  }

  @Setter(AccessLevel.NONE)
  private Map<String, List<String>> genericTagQuery;

  public List<GenericEvent> getReferenceforbelow() {
    return getFilterableListByType("ids");
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
    map.forEach(this::setGenericTagQuery);
  }

  //  TODO: add map content validation?
  @JsonAnySetter
  public void setGenericTagQuery(@NonNull String key, @NonNull List<String> value) {
    getGenericTagQuery().put(key, value);
    GenericTagQuery query = new GenericTagQuery();
    query.setTagName(key);
    query.setValue(value);
    setFilterableListByType(key, List.of(query), GenericTagQueryFilter::new);
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
      @NonNull List<T> filterType,
      @NonNull Function<T, Filterable> filterableFunction) {
    this.core.addFilterList(
        key,
        filterType.stream().map(filterableFunction).collect(Collectors.toList()));
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

    @JsonProperty("ids")
    public FiltersBuilder events(@NonNull List<GenericEvent> events) {
      filters.setEvents(events);
      return this;
    }

    @JsonProperty("authors")
    public FiltersBuilder authors(@NonNull List<PublicKey> authors) {
      filters.setAuthors(authors);
      return this;
    }

    @JsonProperty("kinds")
    public FiltersBuilder kinds(@NonNull List<Kind> kinds) {
      filters.setKinds(kinds);
      return this;
    }

    @JsonProperty("#e")
    public FiltersBuilder referencedEvents(@NonNull List<GenericEvent> events) {
      filters.setReferencedEvents(events);
      return this;
    }

    @JsonProperty("#p")
    public FiltersBuilder referencePubKeys(@NonNull List<PublicKey> publicKeys) {
      filters.setReferencePubKeys(publicKeys);
      return this;
    }

    @JsonProperty("since")
    public FiltersBuilder since(@NonNull Long since) {
      filters.setSince(since);
      return this;
    }

    @JsonProperty("until")
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
