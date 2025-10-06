package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;

@EqualsAndHashCode(callSuper = true)
public class AddressTagFilter<T extends AddressTag> extends AbstractFilterable<T> {
  public static final String FILTER_KEY = "#a";

  public AddressTagFilter(T addressableTag) {
    super(addressableTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        Filterable.getTypeSpecificTags(AddressTag.class, genericEvent).stream()
            .anyMatch(addressTag -> addressTag.equals(getAddressableTag()));
  }

  @Override
  public Object getFilterableValue() {
    String requiredAttributes =
        Stream.of(
                getAddressableTag().getKind(),
                getAddressableTag().getPublicKey().toHexString(),
                getAddressableTag().getIdentifierTag().getUuid())
            .map(Object::toString)
            .collect(Collectors.joining(":"));
    return Optional.ofNullable(getAddressableTag().getRelay())
        .map(relay -> String.join("\",\"", requiredAttributes, relay.getUri()))
        .orElse(requiredAttributes);
  }

  private T getAddressableTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn =
      node -> new AddressTagFilter<>(createAddressTag(node));

  @SuppressWarnings("unchecked")
  protected static <T extends BaseTag> T createAddressTag(@NonNull JsonNode node) {
    String[] nodes = node.asText().split(",");
    List<String> list = Arrays.stream(nodes[0].split(":")).toList();

    final AddressTag addressTag = new AddressTag();
    addressTag.setKind(Integer.valueOf(list.get(0)));
    addressTag.setPublicKey(new PublicKey(list.get(1)));
    addressTag.setIdentifierTag(new IdentifierTag(list.get(2)));

    if (!Objects.equals(2, nodes.length)) return (T) addressTag;

    addressTag.setIdentifierTag(new IdentifierTag(list.get(2).replaceAll("\"$", "")));
    addressTag.setRelay(new Relay(nodes[1].replaceAll("^\"", "")));

    return (T) addressTag;
  }
}
