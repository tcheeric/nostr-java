package nostr.event.tag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import nostr.event.BaseTag;

/**
 * Registry of tag factory functions keyed by tag code. Allows new tag types to be registered
 * without modifying {@link BaseTag}.
 */
public final class TagRegistry {

  private static final Map<String, Function<GenericTag, ? extends BaseTag>> REGISTRY =
      new ConcurrentHashMap<>();

  static {
    register("a", AddressTag::updateFields);
    register("d", IdentifierTag::updateFields);
    register("e", EventTag::updateFields);
    register("g", GeohashTag::updateFields);
    register("l", LabelTag::updateFields);
    register("L", LabelNamespaceTag::updateFields);
    register("p", PubKeyTag::updateFields);
    register("r", ReferenceTag::updateFields);
    register("t", HashtagTag::updateFields);
    register("u", UrlTag::updateFields);
    register("v", VoteTag::updateFields);
    register("emoji", EmojiTag::updateFields);
    register("expiration", ExpirationTag::updateFields);
    register("nonce", NonceTag::updateFields);
    register("price", PriceTag::updateFields);
    register("relays", RelaysTag::updateFields);
    register("subject", SubjectTag::updateFields);
  }

  private TagRegistry() {}

  /**
   * Register a factory function for the supplied tag code.
   *
   * @param code the tag code
   * @param factory factory that creates a {@link BaseTag} from a {@link GenericTag}
   */
  public static void register(String code, Function<GenericTag, ? extends BaseTag> factory) {
    REGISTRY.put(code, factory);
  }

  /**
   * Retrieve the factory function for the given tag code.
   *
   * @param code tag code
   * @return registered factory or {@code null} if none exists
   */
  public static Function<GenericTag, ? extends BaseTag> get(String code) {
    return REGISTRY.get(code);
  }
}
