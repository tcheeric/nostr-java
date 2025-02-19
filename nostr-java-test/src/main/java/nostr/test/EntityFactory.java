package nostr.test;

import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.UserProfile;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Reaction;
import nostr.event.filter.Filters;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.EphemeralEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.impl.InternetIdentifierMetadataEvent;
import nostr.event.impl.MentionsEvent;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.OtsEvent;
import nostr.event.impl.ReactionEvent;
import nostr.event.impl.ReplaceableEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author squirrel
 */
@Log
//TODO - Add the sender PK to all createEvents.
public class EntityFactory {

    @Log
    public static class Events {

        public static EphemeralEvent createEphemeralEvent(PublicKey publicKey) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("eric").build());
            EphemeralEvent event = new EphemeralEvent(publicKey, Kind.EPHEMEREAL_EVENT.getValue(), tagList);
            event.update();
            return event;
        }

        public static DirectMessageEvent createDirectMessageEvent(PublicKey senderPublicKey, PublicKey rcptPublicKey, String content) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(rcptPublicKey).petName("uq7yfx3l").build());
            DirectMessageEvent event = new DirectMessageEvent(senderPublicKey, tagList, content);
            event.update();
            return event;
        }

        public static Filters createFilters(List<PublicKey> authors, List<Kind> kindList, Long since) {
//            return Filters.builder().authors(authors).kinds(kindList).since(since).build();
            return null;
        }

        public static InternetIdentifierMetadataEvent createInternetIdentifierMetadataEvent(UserProfile profile) {
            final PublicKey publicKey = profile.getPublicKey();
            InternetIdentifierMetadataEvent event = new InternetIdentifierMetadataEvent(publicKey, profile);
            event.update();
            return event;
        }

        public static MentionsEvent createMentionsEvent(PublicKey publicKey) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("charlie").build());
            String content = generateRamdomAlpha(32);
            StringBuilder sbContent = new StringBuilder(content);

            int len = tagList.size();
            for (BaseTag baseTag : tagList) {
                sbContent.append(", ").append(((PubKeyTag) baseTag).getPublicKey().toString());

            }
            MentionsEvent event = new MentionsEvent(publicKey, tagList, sbContent.toString());
            event.update();
            return event;
        }

        public static MetadataEvent createMetadataEvent(UserProfile profile) {
            final PublicKey publicKey = profile.getPublicKey();
            return new MetadataEvent(publicKey, profile);
        }

        public static ReactionEvent createReactionEvent(PublicKey publicKey, GenericEvent original) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(EventTag.builder().idEvent(original.getId()).build());
            return new ReactionEvent(publicKey, tagList, Reaction.LIKE);
        }

        public static ReplaceableEvent createReplaceableEvent(PublicKey publicKey) {
            String content = generateRamdomAlpha(32);
            return new ReplaceableEvent(publicKey, 15000, new ArrayList<>(), content);
        }

        public static TextNoteEvent createTextNoteEvent(PublicKey publicKey) {
            String content = generateRamdomAlpha(32);
            return createTextNoteEvent(publicKey, content);
        }

        public static TextNoteEvent createTextNoteEvent(PublicKey publicKey, String content) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("alice").build());
            return new TextNoteEvent(publicKey, tagList, content);
        }

        public static OtsEvent createOtsEvent(PublicKey publicKey, IEvent event) {
            List<BaseTag> tagList = new ArrayList<>();
            final EventTag eventTag = EventTag.builder().idEvent(event.getId()).build();
            tagList.add(eventTag);
            return new OtsEvent(publicKey, tagList, generateRamdomAlpha(32));
        }

        public static GenericTag createGenericTag(PublicKey publicKey) {
            IEvent event = createTextNoteEvent(publicKey);
            return createGenericTag(publicKey, event);
        }

        public static GenericTag createGenericTag(PublicKey publicKey, IEvent event) {
            GenericTag tag = new GenericTag("devil");
            tag.addAttribute(ElementAttribute.builder().name("param0").value("Lucifer").nip(666).build());
            ((GenericEvent) event).addTag(tag);
            return tag;
        }

        public static GenericTag createGenericTag(PublicKey publicKey, IEvent event, Integer tagNip) {
            GenericTag tag = new GenericTag("devil", tagNip);
            tag.addAttribute(ElementAttribute.builder().name("param0").value("Lucifer").nip(666).build());
            ((GenericEvent) event).addTag(tag);
            return tag;
        }

//        public static Filters createFilters(PublicKey publicKey) {
//            List<GenericEvent> eventList = new ArrayList<>();
//            eventList.add(createTextNoteEvent(publicKey));
//            eventList.add(createEphemeralEvent(publicKey));
//
//            List<GenericEvent> refEvents = new ArrayList<>();
//            refEvents.add(createTextNoteEvent(publicKey));
//
//            GenericTagQuery genericTagQuery = createGenericTagQuery();
//            return Filters.builder()
//                .events(eventList)
//                .referencedEvents(refEvents)
//                .genericTagQuery(
//                    Map.of(
//                        genericTagQuery.getTagName(),
//                        genericTagQuery.getValue()))
//                .build();
//        }

        public static List<GenericTagQuery> createGenericTagQuery() {
            Character c = generateRamdomAlpha(1).charAt(0);
            String v1 = generateRamdomAlpha(5);
            String v2 = generateRamdomAlpha(6);
            String v3 = generateRamdomAlpha(7);

            List<String> list = new ArrayList<>();
            list.add(v3);
            list.add(v2);
            list.add(v1);

            return list.stream().map(item -> {
                var result = new GenericTagQuery();
                result.setTagName(c.toString());
                result.setValue(item);
                return result;
            }).toList();
        }
    }

    public static UserProfile createProfile(PublicKey pubKey) {
        try {
            String number = EntityFactory.generateRandomNumber(4);
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
