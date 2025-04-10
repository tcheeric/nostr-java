package nostr.event.json.codec;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import nostr.event.filter.AddressTagFilter;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.GeohashTagFilter;
import nostr.event.filter.HashtagTagFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;
import nostr.event.filter.VoteTagFilter;

public class FilterableProvider {
    protected static List<Filterable> getFilterFunction(@NonNull JsonNode node, @NonNull String type) {
        return switch (type) {
            case ReferencedPublicKeyFilter.FILTER_KEY -> getFilterable(node, ReferencedPublicKeyFilter.fxn);
            case ReferencedEventFilter.FILTER_KEY -> getFilterable(node, ReferencedEventFilter.fxn);
            case IdentifierTagFilter.FILTER_KEY -> getFilterable(node, IdentifierTagFilter.fxn);
            case AddressTagFilter.FILTER_KEY -> getFilterable(node, AddressTagFilter.fxn);
            case GeohashTagFilter.FILTER_KEY -> getFilterable(node, GeohashTagFilter.fxn);
            case HashtagTagFilter.FILTER_KEY -> getFilterable(node, HashtagTagFilter.fxn);
            case VoteTagFilter.FILTER_KEY -> getFilterable(node, VoteTagFilter.fxn);
            case AuthorFilter.FILTER_KEY -> getFilterable(node, AuthorFilter.fxn);
            case EventFilter.FILTER_KEY -> getFilterable(node, EventFilter.fxn);
            case KindFilter.FILTER_KEY -> getFilterable(node, KindFilter.fxn);
            case SinceFilter.FILTER_KEY -> SinceFilter.fxn.apply(node);
            case UntilFilter.FILTER_KEY -> UntilFilter.fxn.apply(node);
            default -> getFilterable(node, GenericTagQueryFilter.fxn(type));
        };
    }

    private static List<Filterable> getFilterable(JsonNode jsonNode, Function<JsonNode, Filterable> filterFunction) {
        return StreamSupport.stream(jsonNode.spliterator(), false).map(filterFunction).toList();
    }
}
