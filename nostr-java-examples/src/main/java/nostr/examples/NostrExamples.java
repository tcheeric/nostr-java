package nostr.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import nostr.base.Channel;
import nostr.base.ContentReason;
import nostr.base.UserProfile;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Marker;
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
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
@Log
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

    public static void main(String[] args) throws IOException, Exception {
        try {
//			Wait it until tried to connect to a half of relays 
            while (CLIENT.getThreadPool().getCompletedTaskCount() < (RELAYS.size() / 2)) {
                Thread.sleep(5000);
            }

            log.log(Level.FINE, "================= The Beginning");
            logAccountsData();

            ExecutorService executor = Executors.newFixedThreadPool(10);

            executor.submit(() -> {
                try {
                    sendTextNoteEvent();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    sendEncryptedDirectMessage();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    mentionsEvent();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    deletionEvent();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    metaDataEvent();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    ephemerealEvent();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    reactionEvent();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    replaceableEvent();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    internetIdMetadata();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    filters();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            });

            executor.submit(() -> {
                try {
                    createChannel();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
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
                try {
                    muteUser();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
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

    private static void sendTextNoteEvent() throws NostrException {
        logHeader("sendTextNoteEvent");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).build();
            List<BaseTag> tags = new ArrayList<>();
            tags.add(rcptTag);

            GenericEvent event = new TextNoteEvent(publicKeySender, tags,
                    "Hello world, I'm here on nostr-java API!");

            SENDER.sign(event);
            BaseMessage message = new EventMessage(event);

            CLIENT.send(message);
        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void sendEncryptedDirectMessage() throws NostrException {
        logHeader("sendEncryptedDirectMessage");

        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).build();
            List<BaseTag> tags = new ArrayList<>();
            tags.add(rcptTag);

            var event2 = new DirectMessageEvent(publicKeySender, tags, "Hello Nakamoto!");

            SENDER.encryptDirectMessage(event2);
            SENDER.sign(event2);

            BaseMessage message = new EventMessage(event2);

            CLIENT.send(message);

        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void mentionsEvent() throws NostrException {
        logHeader("mentionsEvent");

        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
            List<BaseTag> tags = new ArrayList<>();
            tags.add(rcptTag);

            GenericEvent event = new MentionsEvent(publicKeySender, tags, "Hello " + RECEIVER.getPublicKey().toString());
            SENDER.sign(event);

            BaseMessage message = new EventMessage(event);

            CLIENT.send(message);

        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void deletionEvent() throws NostrException {
        logHeader("deletionEvent");

        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
            List<BaseTag> tags = new ArrayList<>();
            tags.add((PubKeyTag) rcptTag);

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

        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void metaDataEvent() throws NostrException {
        logHeader("metaDataEvent");

        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var event = new MetadataEvent(publicKeySender, PROFILE);

            SENDER.sign(event);
            BaseMessage message = new EventMessage(event);

            CLIENT.send(message);

        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void ephemerealEvent() throws NostrException {
        logHeader("ephemerealEvent");

        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
            List<BaseTag> tags = new ArrayList<>();
            tags.add(rcptTag);

            GenericEvent event = new EphemeralEvent(publicKeySender, Kind.EPHEMEREAL_EVENT.getValue(), tags);

            SENDER.sign(event);
            BaseMessage message = new EventMessage(event);

            CLIENT.send(message);
        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void reactionEvent() throws NostrException {
        logHeader("reactionEvent");
        try {
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
            GenericEvent reactionEvent = new ReactionEvent(publicKeySender, tags, Reaction.LIKE, event);

            SENDER.sign(reactionEvent);
            message = new EventMessage(reactionEvent);

            CLIENT.send(message);

        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void replaceableEvent() throws NostrException {
        logHeader("replaceableEvent");
        try {
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
        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private static void internetIdMetadata() throws NostrException {
        logHeader("internetIdMetadata");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).petName("nostr-java").build();
            List<BaseTag> tags = new ArrayList<>();
            tags.add(rcptTag);

            GenericEvent event = new InternetIdentifierMetadataEvent(publicKeySender, tags, PROFILE);

            SENDER.sign(event);
            BaseMessage message = new EventMessage(event);

            CLIENT.send(message);

        } catch (UnsupportedNIPException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    // FIXME
    public static void filters() throws NostrException {
        logHeader("filters");
        try {
            KindList kindList = new KindList();
            kindList.add(Kind.EPHEMEREAL_EVENT);
            kindList.add(Kind.TEXT_NOTE);

            Filters filters = Filters.builder().kinds(kindList).limit(10).build();

            String subId = "subId" + System.currentTimeMillis();
            BaseMessage message = new ReqMessage(subId, filters);

            CLIENT.send(message);
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static GenericEvent createChannel() throws NostrException {
        logHeader("createChannel");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var channel = Channel.builder().name("JNostr Channel")
                    .about("This is a channel to test NIP28 in nostr-java")
                    .picture("https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg").build();
            GenericEvent event = new ChannelCreateEvent(publicKeySender, new ArrayList<BaseTag>(), channel.toString());

            SENDER.sign(event);
            BaseMessage message = new EventMessage(event);

            CLIENT.send(message);

            return event;
        } catch (NostrException ex) {
            throw new NostrException(ex);
        }
    }

    private static void updateChannelMetadata() throws NostrException {
        logHeader("updateChannelMetadata");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var channelCreateEvent = createChannel();

            var tags = new ArrayList<BaseTag>();
            tags.add(EventTag.builder().idEvent(channelCreateEvent.getId())
                    .recommendedRelayUrl(CLIENT.getRelays().stream().findFirst().get().getUri()).build());

            var channel = Channel.builder().name("test change name")
                    .about("This is a channel to test NIP28 in nostr-java | changed")
                    .picture("https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg").build();
            GenericEvent event = new ChannelMetadataEvent(publicKeySender, tags, channel.toString());

            SENDER.sign(event);
            var message = new EventMessage(event);

            CLIENT.send(message);
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static GenericEvent sendChannelMessage() throws NostrException {
        logHeader("sendChannelMessage");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var channelCreateEvent = createChannel();

            var tags = new ArrayList<BaseTag>();
            tags.add(EventTag.builder().idEvent(channelCreateEvent.getId())
                    .recommendedRelayUrl(CLIENT.getRelays().stream().findFirst().get().getUri())
                    .marker(Marker.ROOT)
                    .build());

            GenericEvent event = new ChannelMessageEvent(publicKeySender, tags, "Hello everybody!");

            SENDER.sign(event);
            var message = new EventMessage(event);

            CLIENT.send(message);

            return event;
        } catch (NostrException ex) {
            throw new NostrException(ex);
        }
    }

    private static GenericEvent hideMessage() throws NostrException {
        logHeader("hideMessage");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var channelMessageEvent = sendChannelMessage();

            var tags = new ArrayList<BaseTag>();
            tags.add(EventTag.builder().idEvent(channelMessageEvent.getId()).build());

            GenericEvent event = new HideMessageEvent(publicKeySender, tags,
                    ContentReason.builder().reason("Dick pic").build().toString());

            SENDER.sign(event);
            var message = new EventMessage(event);

            CLIENT.send(message);

            return event;
        } catch (NostrException ex) {
            throw new NostrException(ex);
        }
    }

    private static GenericEvent muteUser() throws NostrException {
        logHeader("muteUser");
        try {
            final PublicKey publicKeySender = SENDER.getPublicKey();

            var tags = new ArrayList<BaseTag>();
            tags.add(PubKeyTag.builder().publicKey(RECEIVER.getPublicKey()).build());

            GenericEvent event = new MuteUserEvent(publicKeySender, tags,
                    ContentReason.builder().reason("Posting dick pics").build().toString());

            SENDER.sign(event);
            var message = new EventMessage(event);

            CLIENT.send(message);

            return event;
        } catch (NostrException ex) {
            throw new NostrException(ex);
        }
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
    private static void logAccountsData() throws NostrException {
        StringBuilder msg = new StringBuilder("################################ ACCOUNTS BEGINNING ################################")
                .append('\n').append("*** RECEIVER ***").append('\n')
                .append('\n').append("* PrivateKey: ").append(RECEIVER.getPrivateKey().getBech32())
                .append('\n').append("* PrivateKey HEX: ").append(RECEIVER.getPrivateKey().toString())
                .append('\n').append("* PublicKey: ").append(RECEIVER.getPublicKey().getBech32())
                .append('\n').append("* PublicKey HEX: ").append(RECEIVER.getPublicKey().toString())
                .append('\n').append('\n').append("*** SENDER ***").append('\n')
                .append('\n').append("* PrivateKey: ").append(SENDER.getPrivateKey().getBech32())
                .append('\n').append("* PrivateKey HEX: ").append(SENDER.getPrivateKey().toString())
                .append('\n').append("* PublicKey: ").append(SENDER.getPublicKey().getBech32())
                .append('\n').append("* PublicKey HEX: ").append(SENDER.getPublicKey().toString())
                .append('\n').append('\n').append("################################ ACCOUNTS END ################################");

        log.log(Level.INFO, msg.toString());
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
