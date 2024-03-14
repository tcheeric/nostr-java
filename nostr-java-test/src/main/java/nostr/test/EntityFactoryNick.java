package nostr.test;

import lombok.extern.java.Log;
import nostr.base.*;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.event.list.EventListNick;
import nostr.event.list.GenericTagQueryList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import nostr.event.tag.PubKeyTag;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author squirrel
 */
@Log
//TODO - Add the sender PK to all createEvents.
public class EntityFactoryNick {

  @Log
  public static class Events {

//    public static EphemeralEvent createEphemeralEvent(PublicKey publicKey) {
//      List<BaseTag> tagList = new ArrayList<>();
//      tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("eric").build());
//      EphemeralEvent event = new EphemeralEvent(new GenericEventImpl(publicKey), Kind.EPHEMEREAL_EVENT.getValue(), tagList);
//      event.update();
//      return event;
//    }

    public static DirectMessageEventNick createDirectMessageEvent(PublicKey senderPublicKey, PublicKey rcptPublicKey, String content) {
      List<BaseTag> tagList = new ArrayList<>();
      tagList.add(PubKeyTag.builder().publicKey(rcptPublicKey).petName("uq7yfx3l").build());
      DirectMessageEventNick event = new DirectMessageEventNick(new GenericEventImpl(senderPublicKey), tagList, content);
      return event;
    }

    public static Filters createFilters(PublicKeyList authors, KindList kindList, Long since) {
      return Filters.builder().authors(authors).kinds(kindList).since(since).build();
    }

//    public static InternetIdentifierMetadataEvent createInternetIdentifierMetadataEvent(UserProfile profile) {
//      final PublicKey publicKey = profile.getPublicKey();
//      InternetIdentifierMetadataEvent event = new InternetIdentifierMetadataEvent(new GenericEventImpl(publicKey), profile);
//      event.update();
//      return event;
//    }

//    public static MentionsEvent createMentionsEvent(PublicKey publicKey) {
//      List<BaseTag> tagList = new ArrayList<>();
//      tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("charlie").build());
//      String content = generateRamdomAlpha(32);
//      StringBuilder sbContent = new StringBuilder(content);
//
//      int len = tagList.size();
//      for (int i = 0; i < len; i++) {
//        sbContent.append(", ").append(((PubKeyTag) tagList.get(i)).getPublicKey().toString());
//
//      }
//      var event = new MentionsEventNick(new GenericEventImpl(publicKey), tagList, sbContent.toString());
//      event.update();
//      return event;
//    }

    public static MetadataEventNick createMetadataEvent(UserProfile profile) {
      return new MetadataEventNick(new GenericEventImpl(profile.getPublicKey()), profile);
    }

//    public static ReactionEvent createReactionEvent(PublicKey publicKey, GenericEvent original) {
//      List<BaseTag> tagList = new ArrayList<>();
//      tagList.add(EventTag.builder().idEvent(original.getId()).build());
//      var event = new ReactionEvent(original);
//      event.setTags(tagList);
//      event.setReaction(Reaction.LIKE);
//      return event;
//    }

    public static ReplaceableEventNick createReplaceableEvent(PublicKey publicKey) {
      String content = generateRamdomAlpha(32);
      var event = new ReplaceableEventNick(new GenericEventImpl(publicKey), Kind.valueOf(15000));
      event.setContent(content);
      return event;
    }

    public static TextNoteEventNick createTextNoteEvent(PublicKey publicKey) {
      String content = generateRamdomAlpha(32);
      return createTextNoteEvent(publicKey, content);
    }

    public static ClassifiedListingEventNick createClassifiedListingEvent(PublicKey publicKey) {
      return ClassifiedListingEventNick.builder()
          .genericEvent(new GenericEventImpl(publicKey))
          .content(generateRamdomAlpha(32))
          .price(List.of(new String[]{"price", "$666", "BTC"}))
          .build();
    }

    public static TextNoteEventNick createTextNoteEvent(PublicKey publicKey, String content) {
      List<BaseTag> tagList = new ArrayList<>();
      tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("alice").build());
      return new TextNoteEventNick(new GenericEventImpl(publicKey, Kind.TEXT_NOTE, tagList, content));
    }

//    public static OtsEvent createOtsEvent(PublicKey publicKey) {
//      List<BaseTag> tagList = new ArrayList<>();
//      final PubKeyTag pkTag = PubKeyTag.builder().publicKey(publicKey).petName("bob").build();
//      tagList.add(pkTag);
//      return new OtsEvent(new GenericEventImpl(publicKey), tagList, generateRamdomAlpha(32), generateRamdomAlpha(32));
//    }

    public static GenericTag createGenericTag(PublicKey publicKey) {
      IEvent event = createTextNoteEvent(publicKey);
      return createGenericTag(publicKey, event);
    }

    public static GenericTag createGenericTag(PublicKey publicKey, IEvent event) {
      GenericTag tag = new GenericTag("devil");
      tag.addAttribute(ElementAttribute.builder().name("param0").value("Lucifer").nip(666).build());
      ((TextNoteEvent) event).addTag(tag);
      return tag;
    }

    public static GenericTag createGenericTag(PublicKey publicKey, IEvent event, Integer tagNip) {
      GenericTag tag = new GenericTag("devil", tagNip);
      tag.addAttribute(ElementAttribute.builder().name("param0").value("Lucifer").nip(666).build());
      ((TextNoteEvent) event).addTag(tag);
      return tag;
    }

    public static FiltersNick createFilters(PublicKey publicKey) {
      EventListNick eventList = new EventListNick();
      eventList.add(createTextNoteEvent(publicKey));
//      eventList.add(createEphemeralEvent(publicKey));

      EventListNick refEvents = new EventListNick();
      refEvents.add(createTextNoteEvent(publicKey));

      GenericTagQueryList gtqList = new GenericTagQueryList();
      gtqList.add(createGenericTagQuery());

      return FiltersNick.builder().events(eventList).referencedEvents(refEvents).genericTagQueryList(gtqList).build();
    }

    public static GenericTagQuery createGenericTagQuery() {
      Character c = generateRamdomAlpha(1).charAt(0);
      String v1 = generateRamdomAlpha(5);
      String v2 = generateRamdomAlpha(6);
      String v3 = generateRamdomAlpha(7);

      List<String> list = new ArrayList<>();
      list.add(v3);
      list.add(v2);
      list.add(v1);

      var result = new GenericTagQuery();
      result.setTagName(c);
      result.setValue(list);
      return result;
    }
  }

  public static UserProfile createProfile(PublicKey pubKey) {
    try {
      String number = EntityFactoryNick.generateRandomNumber(4);
      String about = "about_" + number;
      String name = "name_" + number;
      String nip05 = name + "@tcheeric.com";
      String url = "https://assets.tcheeric.com/" + number + ".PNG";

      return new UserProfile(pubKey, name, nip05, about, new URI(url).toURL());

    } catch (MalformedURLException | URISyntaxException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static String generateRamdomAlpha(int len) {
    return generateRandom(58, 122, len);
  }

  public static String generateRandomNumber(int len) {
    return generateRandom(48, 57, len);
  }

  private static String generateRandom(int leftLimit, int rightLimit, int len) {

    return new Random().ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(len)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

}
