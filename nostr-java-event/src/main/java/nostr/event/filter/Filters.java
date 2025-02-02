package nostr.event.filter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author squirrel
 */
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Filters {
  @JsonIgnore
  private final FiltersCore core;

  public Filters() {
    this.core = new FiltersCore();
  }

  @Key
  @Getter
  @Setter
  private Integer limit;

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
  public Long getSince() {
    return getSinceOrUntil("since");
  }

  @JsonProperty("since")
  public void setSince(@NonNull Long since) {
    this.core.addFilter("since", new SinceFilter<>(since));
  }

  @JsonProperty("until")
  public Long getUntil() {
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
  public Map<String, List<String>> getGenericTagQuery(@NonNull String key) {
    return genericTagQuery;
  }

  @JsonAnySetter
  public void setGenericTagQuery(@NonNull String key, @NonNull List<String> value) {
    this.genericTagQuery = Optional.ofNullable(genericTagQuery).orElse(new HashMap<>());
    this.genericTagQuery.put(key, value);
    GenericTagQuery query = new GenericTagQuery();
    query.setTagName(key);
    query.setValue(value);
    setFilterableListByType(key, List.of(query), GenericTagQueryFilter::new);
  }

  public void setReferencedEventsasdf(@NonNull List<GenericEvent> events) {
    setFilterableListByType("#e", events, EventFilter::new);
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

  private Long getSinceOrUntil(String type) {
    return Optional
        .ofNullable(
            this.core.getFilterableByType(type))
        .stream().flatMap(filterables ->
            filterables.stream().map(dateFilter ->
                (Long) dateFilter.getFilterCriterion()))
        .toList().getFirst();
  }
}
