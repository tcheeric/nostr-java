package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
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

@EqualsAndHashCode(callSuper = true)
public class AddressableTagFilter<T extends AddressTag> extends AbstractFilterable<T> {
  public final static String FILTER_KEY = "#a";

  public AddressableTagFilter(T addressableTag) {
    super(addressableTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return this::compare;
  }

  public static AddressTag createAddressTag(@NonNull JsonNode addressableTag) throws IllegalArgumentException {
    try {
      List<String> list = Arrays.stream(addressableTag.asText().split(":")).toList();

      AddressTag addressTag = new AddressTag();
      addressTag.setKind(Integer.valueOf(list.getFirst()));
      addressTag.setPublicKey(new PublicKey(list.get(1)));
      addressTag.setIdentifierTag(new IdentifierTag(list.get(2)));

      return addressTag;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          String.format("Malformed JsonNode addressable tag: [%s]", addressableTag.asText()), e);
    }
  }

  @Override
  public String getFilterableValue() {
    T addressableTag = getAddressableTag();
    Integer kind = addressableTag.getKind();
    String hexString = addressableTag.getPublicKey().toHexString();
    String id = addressableTag.getIdentifierTag().getId();

    return Stream.of(kind, hexString, id)
        .map(Object::toString)
        .collect(Collectors.joining(":"));
  }

  private boolean compare(@NonNull GenericEvent genericEvent) {
    T addressableTag = getAddressableTag();
    return
        !genericEvent.getPubKey().toHexString().equals(
            addressableTag.getPublicKey().toHexString()) ||
            !genericEvent.getKind().equals(
                addressableTag.getKind()) ||
            getTypeSpecificTags(IdentifierTag.class, genericEvent).stream()
                .anyMatch(identifierTag ->
                    identifierTag.getId().equals(
                        addressableTag.getIdentifierTag().getId()));
  }

  private T getAddressableTag() {
    return super.getFilterable();
  }
}
