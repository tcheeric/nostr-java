package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode
public class AddressableTagFilter<T extends AddressTag> implements Filterable {
  public final static String filterKey = "#a";
  private final T addressableTag;

  public AddressableTagFilter(T addressableTag) {
    this.addressableTag = addressableTag;
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return this::compare;
  }

  private boolean compare(@NonNull GenericEvent genericEvent) {
    return
        !genericEvent.getPubKey().toHexString().equals(
            this.addressableTag.getPublicKey().toHexString()) ||
            !genericEvent.getKind().equals(
                this.addressableTag.getKind()) ||
            getTypeSpecificTags(IdentifierTag.class, genericEvent).stream()
                .anyMatch(identifierTag ->
                    identifierTag.getId().equals(
                        this.addressableTag.getIdentifierTag().getId()));
  }

  @Override
  public T getFilterCriterion() {
    return addressableTag;
  }

  @Override
  public ArrayNode toArrayNode() {
    Integer kind = addressableTag.getKind();
    String hexString = addressableTag.getPublicKey().toHexString();
    String id = addressableTag.getIdentifierTag().getId();
//    String uri = addressableTag.getRelay().getUri();

    String collected = Stream.of(kind, hexString, id).map(Object::toString)
        .collect(Collectors.joining(":"));

    return mapper.createArrayNode().add(collected);
  }

  @Override
  public String getFilterKey() {
    return filterKey;
  }

  public static AddressTag createAddressTag(JsonNode addressableTag) {
    List<String> list = Arrays.stream(addressableTag.asText().split(":")).toList();

//    TODO: add validation
    AddressTag addressTag = new AddressTag();
    addressTag.setKind(Integer.valueOf(list.getFirst()));
    addressTag.setPublicKey(new PublicKey(list.get(1)));
    addressTag.setIdentifierTag(new IdentifierTag(list.get(2)));

    return addressTag;
  }
}
