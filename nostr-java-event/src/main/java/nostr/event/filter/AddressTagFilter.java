package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.CharMatcher;
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
import org.apache.commons.lang3.stream.Streams;

@EqualsAndHashCode(callSuper = true)
public class AddressTagFilter<T extends AddressTag> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "#a";

  public AddressTagFilter(T addressableTag) {
    super(addressableTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        Filterable.getTypeSpecificTags(AddressTag.class, genericEvent).stream()
            .anyMatch(addressTag ->
                addressTag.equals(getAddressableTag()));
  }

  @Override
  public Object getFilterableValue() {
    String collect = Stream.of(
            getAddressableTag().getKind(),
            getAddressableTag().getPublicKey().toHexString(),
            getAddressableTag().getIdentifierTag().getUuid())
        .map(Object::toString).collect(Collectors.joining(":"));
    return Optional.ofNullable(getAddressableTag().getRelay()).map(relay ->
        String.join("\",\"", collect, relay.getUri())).orElse(collect);
  }

  private T getAddressableTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn = node ->
      new AddressTagFilter<>(createAddressTag(node));

  protected static <T extends BaseTag> T createAddressTag(@NonNull JsonNode node) {
    String[] nodes = node.asText().split(",");
    List<String> list = Arrays.stream(nodes[0].split(":")).toList();

    final AddressTag addressTag = new AddressTag();
    addressTag.setKind(Integer.valueOf(list.get(0)));
    addressTag.setPublicKey(new PublicKey(list.get(1)));
    addressTag.setIdentifierTag(new IdentifierTag(list.get(2)));

    if (Objects.equals(2, nodes.length)) {
      String identifierString = CharMatcher.is('"').trimTrailingFrom(list.get(2));
      addressTag.setIdentifierTag(new IdentifierTag(identifierString));
      String relayString = CharMatcher.is('"').trimLeadingFrom(nodes[1]);
      addressTag.setRelay(new Relay(relayString));
    }

    return (T) addressTag;
  }
}
