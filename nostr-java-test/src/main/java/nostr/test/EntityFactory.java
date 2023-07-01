package nostr.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.base.IEvent;
import nostr.base.UserProfile;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Reaction;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.EphemeralEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.impl.InternetIdentifierMetadataEvent;
import nostr.event.impl.MentionsEvent;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.OtsEvent;
import nostr.event.impl.ReactionEvent;
import nostr.event.impl.ReplaceableEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.EventList;
import nostr.event.list.GenericTagQueryList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import nostr.event.tag.PubKeyTag;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Log
//TODO - Add the sender PK to all createEvents.
public class EntityFactory {

    @Log
    public static class Events {

        @SuppressWarnings("unchecked")
        public static EphemeralEvent createEphemeralEvent(PublicKey publicKey) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("eric").build());
            GenericEvent event = new EphemeralEvent(publicKey, Kind.EPHEMEREAL_EVENT.getValue(), tagList);
            event.update();
            return (EphemeralEvent) event;
        }

        public static DirectMessageEvent createDirectMessageEvent(PublicKey senderPublicKey, PublicKey rcptPublicKey, String content) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(rcptPublicKey).petName("uq7yfx3l").build());
            GenericEvent event = new DirectMessageEvent(senderPublicKey, tagList, content);
            event.update();
            return (DirectMessageEvent) event;
        }

        public static Filters createFilters(PublicKeyList authors, KindList kindList, Long since) {
            return Filters.builder().authors(authors).kinds(kindList).since(since).build();
        }

        @SuppressWarnings("unchecked")
        public static InternetIdentifierMetadataEvent createInternetIdentifierMetadataEvent(UserProfile profile) throws NostrException {
            final PublicKey publicKey = profile.getPublicKey();
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("daniel").build());
            GenericEvent event = new InternetIdentifierMetadataEvent(publicKey, tagList, profile);
            event.update();
            return (InternetIdentifierMetadataEvent) event;
        }

        @SuppressWarnings("unchecked")
        public static MentionsEvent createMentionsEvent(PublicKey publicKey) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("charlie").build());
            String content = generateRamdomAlpha(32);
            StringBuilder sbContent = new StringBuilder(content);

            int len = tagList.size();
            for (int i = 0; i < len; i++) {
                sbContent.append(", ").append(((PubKeyTag) tagList.get(i)).getPublicKey().toString());

            }
            GenericEvent event = new MentionsEvent(publicKey, tagList, sbContent.toString());
            event.update();
            return (MentionsEvent) event;
        }

        @SuppressWarnings("unchecked")
        public static MetadataEvent createMetadataEvent(UserProfile profile) throws NostrException {
            final PublicKey publicKey = profile.getPublicKey();
            GenericEvent event = new MetadataEvent(publicKey, profile);
            return (MetadataEvent) event;
        }

        @SuppressWarnings("unchecked")
        public static ReactionEvent createReactionEvent(PublicKey publicKey, GenericEvent original) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("charlie").build());
            GenericEvent event = new ReactionEvent(publicKey, tagList, Reaction.LIKE, original);
            return (ReactionEvent) event;
        }

        public static ReplaceableEvent createReplaceableEvent(PublicKey publicKey) {
            String content = generateRamdomAlpha(32);
            GenericEvent event = new ReplaceableEvent(publicKey, 15000, new ArrayList<>(), content);
            return (ReplaceableEvent) event;
        }

        public static TextNoteEvent createTextNoteEvent(PublicKey publicKey) {
            String content = generateRamdomAlpha(32);
            return createTextNoteEvent(publicKey, content);
        }

        public static TextNoteEvent createTextNoteEvent(PublicKey publicKey, String content) {
            List<BaseTag> tagList = new ArrayList<>();
            tagList.add(PubKeyTag.builder().publicKey(publicKey).petName("alice").build());
            GenericEvent event = new TextNoteEvent(publicKey, tagList, content);
            return (TextNoteEvent) event;
        }

        public static OtsEvent createOtsEvent(PublicKey publicKey) {
            List<BaseTag> tagList = new ArrayList<>();
            final PubKeyTag pkTag = PubKeyTag.builder().publicKey(publicKey).petName("bob").build();
            tagList.add(pkTag);
            OtsEvent event = new OtsEvent(publicKey, tagList, generateRamdomAlpha(32), generateRamdomAlpha(32));
            return event;
        }

        public static GenericTag createGenericTag(PublicKey publicKey) {
            IEvent event = createTextNoteEvent(publicKey);
            return createGenericTag(publicKey, event);
        }

        public static GenericTag createGenericTag(PublicKey publicKey, IEvent event) {
            GenericTag tag = new GenericTag("devil");
            tag.addAttribute(ElementAttribute.builder().value("Lucifer").nip(666).build());
            ((GenericEvent) event).addTag(tag);
            return tag;
        }

        public static GenericTag createGenericTag(PublicKey publicKey, IEvent event, Integer tagNip) {
            GenericTag tag = new GenericTag("devil", tagNip);
            tag.addAttribute(ElementAttribute.builder().value("Lucifer").nip(666).build());
            ((GenericEvent) event).addTag(tag);
            return tag;
        }

        public static Filters createFilters(PublicKey publicKey) {
            EventList eventList = new EventList();
            eventList.add(createTextNoteEvent(publicKey));
            eventList.add(createEphemeralEvent(publicKey));

            EventList refEvents = new EventList();
            refEvents.add(createTextNoteEvent(publicKey));

            GenericTagQueryList gtqList = new GenericTagQueryList();
            gtqList.add(createGenericTagQuery());

            return Filters.builder().events(eventList).referencedEvents(refEvents).genericTagQueryList(gtqList).build();
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
            String number = EntityFactory.generateRandomNumber(4);
            String about = "about_" + number;
            String name = "name_" + number;
            String nip05 = name + "@tcheeric.com";
            String url = "http://assets.tcheeric.com/" + number + ".PNG";

            return new UserProfile(pubKey, name, nip05, about, new URL(url));

        } catch (MalformedURLException ex) {
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
