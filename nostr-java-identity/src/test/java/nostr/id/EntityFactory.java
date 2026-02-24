package nostr.id;

import lombok.extern.slf4j.Slf4j;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author squirrel
 */
@Slf4j
public class EntityFactory {

  @Slf4j
  public static class Events {

    public static GenericEvent createDirectMessageEvent(
        PublicKey senderPublicKey, PublicKey rcptPublicKey, String content) {
      List<BaseTag> tagList = new ArrayList<>();
      tagList.add(BaseTag.create("p", rcptPublicKey.toString()));
      GenericEvent event = new GenericEvent(senderPublicKey, Kinds.ENCRYPTED_DIRECT_MESSAGE, tagList, content);
      event.update();
      return event;
    }

    public static GenericEvent createMentionsEvent(PublicKey publicKey, Integer kind) {
      List<BaseTag> tagList = new ArrayList<>();
      tagList.add(BaseTag.create("p", publicKey.toString()));
      String content = generateRamdomAlpha(32);
      StringBuilder sbContent = new StringBuilder(content);
      sbContent.append(", ").append(publicKey);

      GenericEvent event = new GenericEvent(publicKey, kind, tagList, sbContent.toString());
      event.update();
      return event;
    }

    public static GenericEvent createReactionEvent(PublicKey publicKey, GenericEvent original) {
      List<BaseTag> tagList = new ArrayList<>();
      tagList.add(BaseTag.create("e", original.getId()));
      return new GenericEvent(publicKey, Kinds.REACTION, tagList, "+");
    }

    public static GenericEvent createReplaceableEvent(PublicKey publicKey) {
      String content = generateRamdomAlpha(32);
      return new GenericEvent(publicKey, 15000, new ArrayList<>(), content);
    }

    public static GenericEvent createTextNoteEvent(PublicKey publicKey) {
      String content = generateRamdomAlpha(32);
      return createTextNoteEvent(publicKey, content);
    }

    public static GenericEvent createTextNoteEvent(PublicKey publicKey, String content) {
      List<BaseTag> tagList = new ArrayList<>();
      tagList.add(BaseTag.create("p", publicKey.toString()));
      return new GenericEvent(publicKey, Kinds.TEXT_NOTE, tagList, content);
    }

    public static GenericEvent createOtsEvent(PublicKey publicKey, GenericEvent event) {
      List<BaseTag> tagList = new ArrayList<>();
      tagList.add(BaseTag.create("e", event.getId()));
      return new GenericEvent(publicKey, Kinds.OTS_EVENT, tagList, generateRamdomAlpha(32));
    }

    public static GenericTag createGenericTag(PublicKey publicKey) {
      GenericEvent event = createTextNoteEvent(publicKey);
      return createGenericTag(publicKey, event);
    }

    public static GenericTag createGenericTag(PublicKey publicKey, GenericEvent event) {
      GenericTag tag = GenericTag.of("devil", "Lucifer");
      event.addTag(tag);
      return tag;
    }

  }

  public static String generateRamdomAlpha(int len) {
    return generateRandom(58, 122, len);
  }

  public static String generateRandomNumber(int len) {
    return generateRandom(48, 57, len);
  }

  private static String generateRandom(int leftLimit, int rightLimit, int len) {

    return new Random()
        .ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(len)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
