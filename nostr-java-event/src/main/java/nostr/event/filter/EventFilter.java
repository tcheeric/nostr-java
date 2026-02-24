package nostr.event.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import nostr.event.impl.GenericEvent;
import nostr.event.json.EventJsonMapper;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A composable NIP-01 event filter.
 *
 * <p>Replaces the individual filter wrappers (KindFilter, AuthorFilter, etc.)
 * with a single builder-based filter object.
 */
@Getter
@EqualsAndHashCode
@ToString
public class EventFilter {

  private final List<String> ids;
  private final List<String> authors;
  private final List<Integer> kinds;
  private final Long since;
  private final Long until;
  private final Integer limit;
  private final Map<String, List<String>> tagFilters;

  private EventFilter(Builder builder) {
    this.ids = Collections.unmodifiableList(builder.ids);
    this.authors = Collections.unmodifiableList(builder.authors);
    this.kinds = Collections.unmodifiableList(builder.kinds);
    this.since = builder.since;
    this.until = builder.until;
    this.limit = builder.limit;
    this.tagFilters = Collections.unmodifiableMap(builder.tagFilters);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Predicate<GenericEvent> toPredicate() {
    Predicate<GenericEvent> predicate = e -> true;

    if (!ids.isEmpty()) {
      predicate = predicate.and(e -> ids.contains(e.getId()));
    }
    if (!authors.isEmpty()) {
      predicate = predicate.and(e -> authors.contains(e.getPubKey().toHexString()));
    }
    if (!kinds.isEmpty()) {
      predicate = predicate.and(e -> kinds.contains(e.getKind()));
    }
    if (since != null) {
      predicate = predicate.and(e -> e.getCreatedAt() != null && e.getCreatedAt() > since);
    }
    if (until != null) {
      predicate = predicate.and(e -> e.getCreatedAt() != null && e.getCreatedAt() < until);
    }
    if (!tagFilters.isEmpty()) {
      for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
        String tagCode = entry.getKey();
        List<String> values = entry.getValue();
        predicate = predicate.and(e ->
            e.getTags().stream()
                .filter(GenericTag.class::isInstance)
                .map(GenericTag.class::cast)
                .filter(t -> t.getCode().equals(tagCode))
                .anyMatch(t -> t.getParams().stream().anyMatch(values::contains)));
      }
    }

    return predicate;
  }

  /**
   * Serializes this filter to a JSON object node.
   */
  public ObjectNode toJsonNode() {
    ObjectNode node = EventJsonMapper.getMapper().createObjectNode();

    if (!ids.isEmpty()) {
      ArrayNode arr = node.putArray("ids");
      ids.forEach(arr::add);
    }
    if (!authors.isEmpty()) {
      ArrayNode arr = node.putArray("authors");
      authors.forEach(arr::add);
    }
    if (!kinds.isEmpty()) {
      ArrayNode arr = node.putArray("kinds");
      kinds.forEach(arr::add);
    }
    if (since != null) {
      node.put("since", since);
    }
    if (until != null) {
      node.put("until", until);
    }
    if (limit != null) {
      node.put("limit", limit);
    }
    for (Map.Entry<String, List<String>> entry : tagFilters.entrySet()) {
      ArrayNode arr = node.putArray("#" + entry.getKey());
      entry.getValue().forEach(arr::add);
    }

    return node;
  }

  /**
   * Serializes this filter to a JSON string.
   */
  public String toJson() {
    return toJsonNode().toString();
  }

  /**
   * Deserializes an EventFilter from a JSON string.
   */
  public static EventFilter fromJson(String json) {
    try {
      JsonNode root = EventJsonMapper.getMapper().readTree(json);
      return fromJsonNode(root);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid filter JSON: " + json, e);
    }
  }

  /**
   * Deserializes an EventFilter from a JsonNode.
   */
  public static EventFilter fromJsonNode(JsonNode root) {
    Builder builder = builder();

    if (root.has("ids")) {
      root.get("ids").forEach(n -> builder.ids.add(n.asText()));
    }
    if (root.has("authors")) {
      root.get("authors").forEach(n -> builder.authors.add(n.asText()));
    }
    if (root.has("kinds")) {
      root.get("kinds").forEach(n -> builder.kinds.add(n.asInt()));
    }
    if (root.has("since")) {
      builder.since = root.get("since").asLong();
    }
    if (root.has("until")) {
      builder.until = root.get("until").asLong();
    }
    if (root.has("limit")) {
      builder.limit = root.get("limit").asInt();
    }

    Iterator<String> fieldNames = root.fieldNames();
    while (fieldNames.hasNext()) {
      String field = fieldNames.next();
      if (field.startsWith("#")) {
        String tagCode = field.substring(1);
        List<String> values = new ArrayList<>();
        root.get(field).forEach(n -> values.add(n.asText()));
        builder.tagFilters.put(tagCode, values);
      }
    }

    return builder.build();
  }

  public static class Builder {
    private final List<String> ids = new ArrayList<>();
    private final List<String> authors = new ArrayList<>();
    private final List<Integer> kinds = new ArrayList<>();
    private Long since;
    private Long until;
    private Integer limit;
    private final Map<String, List<String>> tagFilters = new HashMap<>();

    public Builder ids(List<String> ids) { this.ids.addAll(ids); return this; }
    public Builder id(String id) { this.ids.add(id); return this; }
    public Builder authors(List<String> authors) { this.authors.addAll(authors); return this; }
    public Builder author(String author) { this.authors.add(author); return this; }
    public Builder kinds(List<Integer> kinds) { this.kinds.addAll(kinds); return this; }
    public Builder kind(int kind) { this.kinds.add(kind); return this; }
    public Builder since(long since) { this.since = since; return this; }
    public Builder until(long until) { this.until = until; return this; }
    public Builder limit(int limit) { this.limit = limit; return this; }
    public Builder addTagFilter(String tagCode, List<String> values) {
      this.tagFilters.computeIfAbsent(tagCode, k -> new ArrayList<>()).addAll(values);
      return this;
    }
    public Builder addTagFilter(String tagCode, String value) {
      this.tagFilters.computeIfAbsent(tagCode, k -> new ArrayList<>()).add(value);
      return this;
    }

    public EventFilter build() {
      return new EventFilter(this);
    }
  }
}
