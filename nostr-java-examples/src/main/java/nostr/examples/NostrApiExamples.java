package nostr.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;

import lombok.extern.java.Log;
import nostr.api.NIP01;
import nostr.api.NIP04;
import nostr.api.NIP05;
import nostr.api.NIP08;
import nostr.api.NIP09;
import nostr.api.NIP16;
import nostr.api.NIP25;
import nostr.api.NIP28;
import nostr.api.Nostr;
import nostr.base.ChannelProfile;
import nostr.base.UserProfile;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Reaction;
import nostr.event.impl.ChannelCreateEvent;
import nostr.event.impl.ChannelMessageEvent;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.MentionsEvent;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.KindList;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
@Log
public class NostrApiExamples {

    private static final Identity RECIPIENT = Identity.generateRandomIdentity();
    private static final Identity SENDER = Identity.generateRandomIdentity();

    private static final UserProfile PROFILE = new UserProfile(SENDER.getPublicKey(), "Nostr Guy", "guy@nostr-java.io", "It's me!", null);
	private final static Map<String, String> RELAYS = Map.of("brb", "brb.io", "damus", "relay.damus.io", "ZBD",
			"nostr.zebedee.cloud", "taxi", "relay.taxi", "vision", "relay.nostr.vision");

    static {
        final LogManager logManager = LogManager.getLogManager();
        try (final InputStream is = NostrApiExamples.class
				.getResourceAsStream("/logging.properties")) {
			logManager.readConfiguration(is);
        } catch (IOException ex) {
            System.exit(-1000);
        }

        try {
            PROFILE.setPicture(new URI("https://images.unsplash.com/photo-1462888210965-cdf193fb74de").toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            log.log(Level.FINE, "================= The Beginning");
            logAccountsData();

            ExecutorService executor = Executors.newFixedThreadPool(10);

//			executor.submit(() -> {
//				metaDataEvent();
//			});
//
//			executor.submit(() -> {
//				sendTextNoteEvent();
//			});
//
//            executor.submit(() -> {
//                sendEncryptedDirectMessage();
//            });

            executor.submit(() -> {
                mentionsEvent();
            });

//            executor.submit(() -> {
//                deletionEvent();
//            });
//
//            executor.submit(() -> {
//                ephemerealEvent();
//            });
//
//            executor.submit(() -> {
//                reactionEvent();
//            });
//
//            executor.submit(() -> {
//                replaceableEvent();
//            });
//
//            executor.submit(() -> {
//                internetIdMetadata();
//            });
//
//            executor.submit(() -> {
//                filters();
//            });
//
//            executor.submit(() -> {
//                createChannel();
//            });
//            executor.submit(() -> {
//                updateChannelMetadata();
//            });
//
//            executor.submit(() -> {
//                sendChannelMessage();
//            });
//
//            executor.submit(() -> {
//                hideMessage();
//            });
//
//            executor.submit(() -> {
//                muteUser();
//            });

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

        PubKeyTag rcptTag = PubKeyTag.builder().publicKey(RECIPIENT.getPublicKey()).build();
        List<BaseTag> tags = new ArrayList<>();
        tags.add(rcptTag);

        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        nip01.createTextNoteEvent(tags, "Hello world, I'm here on nostr-java API!")
        		.sign()
        		.send(RELAYS);
    }

    private static void sendEncryptedDirectMessage() {
        logHeader("sendEncryptedDirectMessage");

        var nip04 = new NIP04<DirectMessageEvent>(SENDER, RECIPIENT.getPublicKey());
        nip04.createDirectMessageEvent("Hello Nakamoto!")
			.encrypt()
			.sign()
			.send(RELAYS);
    }

    private static void mentionsEvent() {
        logHeader("mentionsEvent");

        List<BaseTag> tags = List.of(new PubKeyTag(RECIPIENT.getPublicKey()));

        var nip08 = new NIP08<MentionsEvent>(SENDER);
        nip08.createMentionsEvent(tags, "Hello #[0]")
			.sign()
			.send(RELAYS);
    }

    private static void deletionEvent() {
        logHeader("deletionEvent");

        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        var event = nip01.createTextNoteEvent("Hello Astral, Please delete me!")
        		.sign()
        		.send(RELAYS);

        List<BaseTag> tags = new ArrayList<>();
        tags.add(EventTag.builder().idEvent(event.getId()).build());
        var delEvent = NIP09.createDeletionEvent(tags);

		Nostr.getInstance().sign(delEvent).send(delEvent);
    }

    private static MetadataEvent metaDataEvent() {
        logHeader("metaDataEvent");

        var nip01 = new NIP01<MetadataEvent>(SENDER);
        nip01.createMetadataEvent(PROFILE)
        		.sign()
        		.send(RELAYS);
        
        return nip01.getEvent();
    }

    private static void ephemerealEvent() {
        logHeader("ephemerealEvent");

        var event = NIP16.createEphemeralEvent(Integer.SIZE, "An ephemereal event");
		Nostr.getInstance().sign(SENDER, event).send(event);
    }

    private static void reactionEvent() {
        logHeader("reactionEvent");

        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        var event = nip01.createTextNoteEvent("Hello Astral, Please like me!")
        		.sign()
        		.send(RELAYS);

        var reactionEvent = NIP25.createReactionEvent(event, Reaction.LIKE);
		Nostr.getInstance().sign(reactionEvent).send(reactionEvent);
    }

    private static void replaceableEvent() {
        logHeader("replaceableEvent");

        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        var event = nip01.createTextNoteEvent("Hello Astral, Please replace me!")
        		.sign()
        		.send(RELAYS);

        List<BaseTag> tags = new ArrayList<>();
        tags.add(EventTag.builder().idEvent(event.getId()).build());
        var replaceableEvent = NIP16.createReplaceableEvent(tags, 15_000, "New content");
		Nostr.getInstance().sign(replaceableEvent).send(replaceableEvent);
    }

    private static void internetIdMetadata() {
        logHeader("internetIdMetadata");

        var event = NIP05.createInternetIdentifierMetadataEvent(PROFILE);
		Nostr.getInstance().sign(SENDER, event).send(event);
    }

    public static void filters() {
        logHeader("filters");

        KindList kindList = new KindList();
        kindList.add(Kind.EPHEMEREAL_EVENT.getValue());
        kindList.add(Kind.TEXT_NOTE.getValue());

        Filters filters = NIP01.createFilters(null, null, kindList, null, null, null, null, null, null);
        String subId = "subId" + System.currentTimeMillis();
		Nostr.getInstance().send(filters, subId);
    }

    private static GenericEvent createChannel() {
        try {
            logHeader("createChannel");
            
            var channel = new ChannelProfile("JNostr Channel", "This is a channel to test NIP28 in nostr-java", "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg");
            var event = NIP28.createChannelCreateEvent(channel);
            
			Nostr.getInstance().sign(SENDER, event).send(event);
            
            return event;
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void updateChannelMetadata() {
        try {
            logHeader("updateChannelMetadata");
            
            var channelCreateEvent = createChannel();
            
            BaseTag tag = EventTag.builder()
                    .idEvent(channelCreateEvent.getId())
                    .recommendedRelayUrl("localhost:8080")
                    .build();
            
            var channel = new ChannelProfile("test change name", "This is a channel to test NIP28 in nostr-java | changed", "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg");
            var event = NIP28.createChannelCreateEvent(channel);
            event.addTag(tag);
            
			Nostr.getInstance().sign(SENDER, event).send(event);
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

    }

    private static GenericEvent sendChannelMessage() {
        logHeader("sendChannelMessage");

        var channelCreateEvent = createChannel();

        GenericEvent event = NIP28.createChannelMessageEvent((ChannelCreateEvent) channelCreateEvent, "Hello everybody!");
		Nostr.getInstance().sign(SENDER, event).send(event);

        return event;
    }

    private static GenericEvent hideMessage() {
        logHeader("hideMessage");

        var channelMessageEvent = sendChannelMessage();

        GenericEvent event = NIP28.createHideMessageEvent((ChannelMessageEvent) channelMessageEvent, "Dick pic");

		Nostr.getInstance().sign(SENDER, event).send(event);

        return event;
    }

    private static GenericEvent muteUser() {
        logHeader("muteUser");

        GenericEvent event = NIP28.createMuteUserEvent(RECIPIENT.getPublicKey(), "Posting dick pics");

		Nostr.getInstance().sign(SENDER, event).send(event);

        return event;
    }

    private static void logAccountsData() {
        String msg = "################################ ACCOUNTS BEGINNING ################################" +
                '\n' + "*** RECEIVER ***" + '\n' +
                '\n' + "* PrivateKey: " + RECIPIENT.getPrivateKey().getBech32() +
                '\n' + "* PrivateKey HEX: " + RECIPIENT.getPrivateKey().toString() +
                '\n' + "* PublicKey: " + RECIPIENT.getPublicKey().getBech32() +
                '\n' + "* PublicKey HEX: " + RECIPIENT.getPublicKey().toString() +
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
