package nostr.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
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
import nostr.api.NIP25;
import nostr.api.NIP28;
import nostr.api.NIP30;
import nostr.api.Nostr;
import nostr.base.ChannelProfile;
import nostr.base.PublicKey;
import nostr.base.UserProfile;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Reaction;
import nostr.event.impl.ChannelCreateEvent;
import nostr.event.impl.ChannelMessageEvent;
import nostr.event.impl.DeletionEvent;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.EphemeralEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.InternetIdentifierMetadataEvent;
import nostr.event.impl.MentionsEvent;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.ReactionEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
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
	private final static Map<String, String> RELAYS = Map.of("lol", "nos.lol", "damus", "relay.damus.io", "ZBD",
			"nostr.zebedee.cloud", "taxi", "relay.taxi", "mom", "nostr.mom");

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
//	        	try {
//					metaDataEvent();
//	        	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//			});

//			executor.submit(() -> {
//	        	try {
//					sendTextNoteEvent();
//	        	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//			});

//            executor.submit(() -> {
//		    	try {
//		            sendEncryptedDirectMessage();
//		    	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//            });

//            executor.submit(() -> {
//	        	try {
//	                mentionsEvent();
//	        	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//            });

//            executor.submit(() -> {
//	        	try {
//	                deletionEvent();
//	        	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//            });
//
//            executor.submit(() -> {
//	    		try {
//		            ephemerealEvent();
//				} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//            });
//
//            executor.submit(() -> {
//            	try {
//                	reactionEvent();
//            	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//            });

//            executor.submit(() -> {
//	        	try {
//	        		replaceableEvent();
//	        	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//            });
//
//            executor.submit(() -> {
//	        	try {
//	                internetIdMetadata();
//	        	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
//            });
//
            executor.submit(() -> {
            	try {
            		filters();
	        	} catch(Throwable t) { log.log(Level.SEVERE, t.getMessage(), t); };
            });
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

    private static TextNoteEvent sendTextNoteEvent() {
        logHeader("sendTextNoteEvent");

        List<BaseTag> tags = new ArrayList<>(List.of(new PubKeyTag(RECIPIENT.getPublicKey())));

        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        nip01.createTextNoteEvent(tags, "Hello world, I'm here on nostr-java API!")
        		.sign()
        		.send(RELAYS);
        
        return nip01.getEvent();
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

        List<BaseTag> tags = new ArrayList<>(List.of(new PubKeyTag(RECIPIENT.getPublicKey())));

        var nip08 = new NIP08<MentionsEvent>(SENDER);
        nip08.createMentionsEvent(tags, "Hello #[0]")
			.sign()
			.send(RELAYS);
    }

	private static void deletionEvent() {
		logHeader("deletionEvent");

		var event = sendTextNoteEvent();
		List<BaseTag> tags = new ArrayList<>(List.of(new EventTag(event.getId())));

		var nip09 = new NIP09<DeletionEvent>(SENDER);
		nip09.createDeletionEvent(tags)
			.sign()
			.send();
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
        logHeader("ephemeralEvent");

        var nip01 = new NIP01<EphemeralEvent>(SENDER);
        nip01.createEphemeralEvent(21000, "An ephemeral event")
			.sign()
			.send(RELAYS);
    }

    private static void reactionEvent() {
        logHeader("reactionEvent");

        List<BaseTag> tags = new ArrayList<>(List.of(NIP30.createCustomEmojiTag("soapbox", "https://gleasonator.com/emoji/Gleasonator/soapbox.png")));
        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        var event = nip01.createTextNoteEvent(tags, "Hello Astral, Please like me! :soapbox:")
        		.signAndSend(RELAYS);
        		
        var nip25 = new NIP25<ReactionEvent>(RECIPIENT); 
        nip25.createReactionEvent(event, Reaction.LIKE).signAndSend(RELAYS);
        nip25.createReactionEvent(event, "💩").signAndSend();
//        Using Custom Emoji as reaction 
        nip25.createReactionEvent(event, NIP30.createCustomEmojiTag("ablobcatrainbow", "https://gleasonator.com/emoji/blobcat/ablobcatrainbow.png"))
        	.signAndSend();
    }

    private static void replaceableEvent() {
        logHeader("replaceableEvent");

        var nip01 = new NIP01<TextNoteEvent>(SENDER);
        var event = nip01.createTextNoteEvent("Hello Astral, Please replace me!").signAndSend(RELAYS);

        nip01.createReplaceableEvent(List.of(new EventTag(event.getId())), 15_000, "New content")
			.signAndSend();
    }

    private static void internetIdMetadata() {
        logHeader("internetIdMetadata");
        var profile = UserProfile.builder()
        		.name("Guilherme Gps")
        		.publicKey(new PublicKey("21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144"))
        		.nip05("me@guilhermegps.com.br")
        		.build();

        var nip05 = new NIP05<InternetIdentifierMetadataEvent>(SENDER);
        nip05.createInternetIdentifierMetadataEvent(profile)
        	.sign()
        	.send(RELAYS);
    }

    public static void filters() throws InterruptedException {
        logHeader("filters");

        var kinds = new KindList(List.of(Kind.EPHEMEREAL_EVENT.getValue(), Kind.TEXT_NOTE.getValue()));
        var authors = new PublicKeyList(List.of(new PublicKey("21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144")));

        var date = Calendar.getInstance();
        var subId = "subId" + date.getTimeInMillis();
        date.add(Calendar.DAY_OF_MONTH, -5);
        Filters filters = Filters.builder()
        		.authors(authors)
        		.kinds(kinds)
        		.since(date.getTimeInMillis()/1000)
        		.build();
        
        var nip01 = NIP01.getInstance();
        nip01.setRelays(RELAYS).send(filters, subId);
        Thread.sleep(5000);        
		nip01.responses();			
    }

    private static GenericEvent createChannel() {
        try {
            logHeader("createChannel");
            
            var channel = new ChannelProfile("Nostr-java Channel", "This is a channel to test NIP28 in nostr-java", "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg");
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
