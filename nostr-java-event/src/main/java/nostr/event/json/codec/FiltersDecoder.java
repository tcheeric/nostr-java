package nostr.event.json.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.filter.AddressableTagFilter;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.GeohashTagFilter;
import nostr.event.filter.HashtagTagFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * @author eric
 */
@Data
public class FiltersDecoder implements FDecoder<Filters> {
  private final static ObjectMapper mapper = new ObjectMapper();

  @SneakyThrows
  public Filters decode(@NonNull String jsonFiltersList) {
    final List<Filterable> filterables = new ArrayList<>();

    mapper.readTree(jsonFiltersList).fields().forEachRemaining(node ->
        filterables.addAll(
            FilterableProvider.getFilterFunction(
                node.getValue(),
                node.getKey())));

    return new Filters(filterables);
  }
}
