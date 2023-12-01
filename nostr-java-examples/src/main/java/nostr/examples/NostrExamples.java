package nostr.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;

import lombok.extern.java.Log;
import nostr.base.ChannelProfile;
import nostr.base.ContentReason;
import nostr.base.UserProfile;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Reaction;
import nostr.event.impl.ChannelCreateEvent;
import nostr.event.impl.ChannelMessageEvent;
import nostr.event.impl.ChannelMetadataEvent;
import nostr.event.impl.DeletionEvent;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.EphemeralEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.HideMessageEvent;
import nostr.event.impl.InternetIdentifierMetadataEvent;
import nostr.event.impl.MentionsEvent;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.MuteUserEvent;
import nostr.event.impl.ReactionEvent;
import nostr.event.impl.ReplaceableEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.KindList;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.client.Client;
import nostr.id.Identity;
import nostr.id.IdentityHelper;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
@Log
@Deprecated
public class NostrExamples {

    private static final Identity RECEIVER = Identity.generateRandomIdentity();
    private static final Identity SENDER = Identity.generateRandomIdentity();

    private static final UserProfile PROFILE = new UserProfile(SENDER.getPublicKey(), "erict875", "erict875@nostr-java.io", "It's me!", null);

    private final static Map<String, String> RELAYS = Map.of("brb", "brb.io", "damus", "relay.damus.io", "ZBD", "nostr.zebedee.cloud", "taxi", "relay.taxi", "vision", "relay.nostr.vision");

    private final static Client CLIENT = Client.getInstance(RELAYS);

    static {
        final LogManager logManager = LogManager.getLogManager();
        try (final InputStream is = NostrExamples.class
                .getResourceAsStream("/logging.properties")) {
            logManager.readConfiguration(is);
        } catch (IOException ex) {
            System.exit(-1000);
        }

        try {
            PROFILE.setPicture(new URL("https://images.unsplash.com/photo-1462888210965-cdf193fb74de"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
//			Wait it until tried to connect to a half of relays 
            while (CLIENT.getThreadPool().getCompletedTaskCount() < (RELAYS.size() / 2)) {
                Thread.sleep(5000);
            }

            log.log(Level.FINE, "================= The Beginning");
            logAccountsData();

            ExecutorService executor = Executors.newFixedThreadPool(10);

            executor.submit(() -> {
                sendTextNoteEvent();
            });

            executor.submit(() -> {
                try {
                    sendEncryptedDirectMessage();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                mentionsEvent();
            });

            executor.submit(() -> {
                deletionEvent();
            });

            executor.submit(() -> {
                metaDataEvent();
            });

            executor.submit(() -> {
                ephemerealEvent();
            });

            executor.submit(() -> {
                reactionEvent();
            });

            executor.submit(() -> {
                replaceableEvent();
            });

            executor.submit(() -> {
                internetIdMetadata();
            });

            executor.submit(() -> {
                try {
                    filters();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                createChannel();
            });

            executor.submit(() -> {
                try {
                    updateChannelMetadata();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    sendChannelMessage();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    hideMessage();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                muteUser();
            });

            stop(executor);

            if (executor.isTerminated()) {
                log.log(Level.FINE, "================== The End");
            }

        } catch (IllegalArgumentException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }
    }

    private static void sendTextNoteEvent() {
        logHeader("sendTextNoteEvent");
        final PublicKey publicKeySender = SENDER.getPublicKey();

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        GenericEvent event = new TextNoteEvent(publicKeySender, tags,
                "Hello world, I'm here on nostr-java API!");

        SENDER.sign(event);
        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);
    }

    private static void sendEncryptedDirectMessage() throws NostrException {
        logHeader("sendEncryptedDirectMessage");

        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).build();
            List<BaseTag> tags = new ArrayList<>();
            tags.add(rcptTag);

            var event2 = new DirectMessageEvent(publicKeySender, tags, "Hello Nakamoto!");

            new IdentityHelper(SENDER).encryptDirectMessage(event2);
            SENDER.sign(event2);

            BaseMessage message = new EventMessage(event2);

            CLIENT.send(message);

        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void mentionsEvent() {
        logHeader("mentionsEvent");

        final PublicKey publicKeySender = SENDER.getPublicKey();

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        GenericEvent event = new MentionsEvent(publicKeySender, tags, "Hello " + RECEIVER.getPublicKey().toString());
        SENDER.sign(event);

        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);

    }

    private static void deletionEvent() {
        logHeader("deletionEvent");

        final PublicKey publicKeySender = SENDER.getPublicKey();

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        GenericEvent event = new TextNoteEvent(publicKeySender, tags, "Hello Astral, Please delete me!");

        SENDER.sign(event);
        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);

        tags = new ArrayList<>();
        tags.add(EventTag.builder().idEvent(event.getId()).build());
        GenericEvent delEvent = new DeletionEvent(publicKeySender, tags);

        SENDER.sign(delEvent);
        message = new EventMessage(delEvent);

        CLIENT.send(message);

    }

    private static void metaDataEvent() {
        logHeader("metaDataEvent");

        final PublicKey publicKeySender = SENDER.getPublicKey();

        var event = new MetadataEvent(publicKeySender, PROFILE);

        SENDER.sign(event);
        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);

    }

    private static void ephemerealEvent() {
        logHeader("ephemerealEvent");

        final PublicKey publicKeySender = SENDER.getPublicKey();

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        GenericEvent event = new EphemeralEvent(publicKeySender, Kind.EPHEMEREAL_EVENT.getValue(), tags);

        SENDER.sign(event);
        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);
    }

    private static void reactionEvent() {
        logHeader("reactionEvent");
        final PublicKey publicKeySender = SENDER.getPublicKey();

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        GenericEvent event = new TextNoteEvent(publicKeySender, tags, "Hello Astral, Please like me!");

        SENDER.sign(event);
        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);

        tags = new ArrayList<>();
        tags.add(EventTag.builder().idEvent(event.getId()).build());
        tags.add(PubKeyTag.builder().publicKey(publicKeySender).build());
        GenericEvent reactionEvent = new ReactionEvent(publicKeySender, tags, Reaction.LIKE);

        SENDER.sign(reactionEvent);
        message = new EventMessage(reactionEvent);

        CLIENT.send(message);

    }

    private static void replaceableEvent() {
        logHeader("replaceableEvent");
        final PublicKey publicKeySender = SENDER.getPublicKey();

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        GenericEvent event = new TextNoteEvent(publicKeySender, tags, "Hello Astral, Please replace me!");

        SENDER.sign(event);
        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);

        tags = new ArrayList<>();
        tags.add(EventTag.builder().idEvent(event.getId()).build());
        GenericEvent replaceableEvent = new ReplaceableEvent(publicKeySender, 15000, tags, "Content");

        SENDER.sign(replaceableEvent);
        message = new EventMessage(replaceableEvent);

        CLIENT.send(message);

        replaceableEvent = new ReplaceableEvent(publicKeySender, 15000, tags, "New Content");

        SENDER.sign(replaceableEvent);
        message = new EventMessage(replaceableEvent);

        CLIENT.send(message);
    }

    private static void internetIdMetadata() {
        logHeader("internetIdMetadata");

        final PublicKey publicKeySender = SENDER.getPublicKey();

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        GenericEvent event = new InternetIdentifierMetadataEvent(publicKeySender, PROFILE);

        SENDER.sign(event);
        BaseMessage message = new EventMessage(event);

        CLIENT.send(message);

    }

    // FIXME
    public static void filters() throws NostrException {
        logHeader("filters");
        try {
            KindList kindList = new KindList();
            kindList.add(Kind.EPHEMEREAL_EVENT.getValue());
            kindList.add(Kind.TEXT_NOTE.getValue());

            Filters filters = Filters.builder().kinds(kindList).limit(10).build();

            String subId = "subId" + System.currentTimeMillis();
            BaseMessage message = new ReqMessage(subId, filters);

            CLIENT.send(message);
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static GenericEvent createChannel() {
        logHeader("createChannel");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var channel = new ChannelProfile("JNostr Channel", "This is a channel to test NIP28 in nostr-java", "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg");
            var event = new ChannelCreateEvent(publicKeySender, channel);

            SENDER.sign(event);
            BaseMessage message = new EventMessage(event);

            CLIENT.send(message);

            return event;
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void updateChannelMetadata() throws NostrException {
        logHeader("updateChannelMetadata");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var channelCreateEvent = createChannel();

            var channel = new ChannelProfile("JNostr Channel | changed", "This is a channel to test NIP28 in nostr-java | changed", "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg");

            GenericEvent event = new ChannelMetadataEvent(publicKeySender, (ChannelCreateEvent) channelCreateEvent, channel);

            SENDER.sign(event);
            var message = new EventMessage(event);

            CLIENT.send(message);
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new NostrException(ex);
        }
    }

    private static GenericEvent sendChannelMessage() throws NostrException {
        logHeader("sendChannelMessage");
        final PublicKey publicKeySender = SENDER.getPublicKey();

        var channelCreateEvent = createChannel();

        var event = new ChannelMessageEvent(publicKeySender, (ChannelCreateEvent) channelCreateEvent, "Hello everybody!");

        SENDER.sign(event);
        var message = new EventMessage(event);

        CLIENT.send(message);

        return event;
    }

    private static GenericEvent hideMessage() throws NostrException {
        logHeader("hideMessage");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var channelMessageEvent = sendChannelMessage();

            GenericEvent event = new HideMessageEvent(publicKeySender, (ChannelMessageEvent) channelMessageEvent,
                    ContentReason.builder().reason("Dick pic").build().toString());

            SENDER.sign(event);
            var message = new EventMessage(event);

            CLIENT.send(message);

            return event;
        } catch (NostrException ex) {
            throw new NostrException(ex);
        }
    }

    private static GenericEvent muteUser() {
        logHeader("muteUser");
        final PublicKey publicKeySender = SENDER.getPublicKey();

        GenericEvent event = new MuteUserEvent(publicKeySender, RECEIVER.getPublicKey(),
                ContentReason.builder().reason("Posting dick pics").build().toString());

        SENDER.sign(event);
        var message = new EventMessage(event);

        CLIENT.send(message);

        return event;
    }

//    public static void sensitiveContentNote(Identity wallet, Client cliepublicKeySendernt) throws NostrException {
//        logHeader("sensitiveContentNote");
//        try {
//            // Create the attribute value list            
//            List<String> values = new ArrayList<>();            
//            values.add("sensitive content");
//            
//            // Create the attributes
//            final ElementAttribute attr = ElementAttribute.builder().nip(36).isString(true).name("reason").valueList(values).build();                        
//            Set<ElementAttribute> attributes = new HashSet<>();
//            attributes.add(attr);
//            
//            GenericTag sensitiveContentTag = new GenericTag(1, "", attributes);
//        } catch (UnsupportedNIPException ex) {
//                        log.log(Level.WARNING, null, ex);
//        } catch (Exception ex) {
//            throw new NostrException(ex);
//        }
//
//    }
    private static void logAccountsData() {
        String msg = "################################ ACCOUNTS BEGINNING ################################" +
                '\n' + "*** RECEIVER ***" + '\n' +
                '\n' + "* PrivateKey: " + RECEIVER.getPrivateKey().getBech32() +
                '\n' + "* PrivateKey HEX: " + RECEIVER.getPrivateKey().toString() +
                '\n' + "* PublicKey: " + RECEIVER.getPublicKey().getBech32() +
                '\n' + "* PublicKey HEX: " + RECEIVER.getPublicKey().toString() +
                '\n' + '\n' + "*** SENDER ***" + '\n' +
                '\n' + "* PrivateKey: " + SENDER.getPrivateKey().getBech32() +
                '\n' + "* PrivateKey HEX: " + SENDER.getPrivateKey().toString() +
                '\n' + "* PublicKey: " + SENDER.getPublicKey().getBech32() +
                '\n' + "* PublicKey HEX: " + SENDER.getPublicKey().toString() +
                '\n' + '\n' + "################################ ACCOUNTS END ################################";

        log.log(Level.INFO, msg);
    }

    private static void logHeader(String header) {
        for (int i = 0; i < 30; i++) {
            System.out.print("#");
        }
        System.out.println();
        System.out.println("\t" + header);
        for (int i = 0; i < 30; i++) {
            System.out.print("#");
        }
        System.out.println();
    }

    private static void stop(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, "termination interrupted");
        } finally {
            if (!executor.isTerminated()) {
                log.log(Level.SEVERE, "killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }
}
