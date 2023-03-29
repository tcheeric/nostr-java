package nostr.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.java.Log;
import nostr.base.Channel;
import nostr.base.ContentReason;
import nostr.base.ITag;
import nostr.base.PrivateKey;
import nostr.base.Profile;
import nostr.base.PublicKey;
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
import nostr.event.impl.GenericMessage;
import nostr.event.impl.HideMessageEvent;
import nostr.event.impl.InternetIdentifierMetadataEvent;
import nostr.event.impl.MentionsEvent;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.MuteUserEvent;
import nostr.event.impl.ReactionEvent;
import nostr.event.impl.ReplaceableEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.KindList;
import nostr.event.list.PubKeyTagList;
import nostr.event.list.TagList;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Client;
import nostr.id.Identity;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
@Log
public class NostrExamples {

	/**
	 * Private key of the public key, in case you want to check the messages:
	 * 
	 * nsec1yjs4nalp47mwhvjwg0ne7gltwcv8g8glzhsucnmyujdvr87hda8qkjl88s
	 * 24a159f7e1afb6ebb24e43e79f23eb7618741d1f15e1cc4f64e49ac19fd76f4e
	 */
	private final static String PUBLIC_KEY = "98fd512949146f36fe4e84ee0c68e6f04780c7037c6e2cf8baf74033ccd1b687";
	private final static Profile profile = Profile.builder()
    		.name("test")
    		.about("Hey, it's me!")
    		.publicKey(new PublicKey("99cf4426cb4507688ff151a760ec098ff78af3cfcdcb6e74fa9c9ed76cba43fa"))
    		.build();
	private final static Identity identity = new Identity(profile, new PrivateKey("04a7dd63ef4dfd4ab95ff8c1576b1d252831a0c53f13657d959a199b4de4b670"));
	private final static Map<String, String> relays = Stream.of(new String[][] {
			{ "brb", "brb.io" },
			{ "ZBD", "nostr.zebedee.cloud" },
			{ "damus", "relay.damus.io" },
			{ "taxi", "relay.taxi" },
			{ "relay.nostr.vision", "relay.nostr.vision" }
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
	private final static Client client = new Client("test", identity, relays);

	static {
		final LogManager logManager = LogManager.getLogManager();
		try (final InputStream is = NostrExamples.class.getResourceAsStream("/logging.properties")) {
			logManager.readConfiguration(is);
		} catch (IOException ex) {
			System.exit(-1000);
		}

		try {
			profile.setPicture(new URL("https://images.unsplash.com/photo-1462888210965-cdf193fb74de"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, Exception {
		try {

			log.log(Level.FINE, "================= The Beginning");

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
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			TagList tagList = new TagList();
			tagList.add(pkSenderTag);

			GenericEvent event = new TextNoteEvent(publicKeySender, tagList,
					"Hello world, I'm here on nostr-java API!");

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);
		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void sendEncryptedDirectMessage() throws NostrException {
		logHeader("sendEncryptedDirectMessage");

		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();
			PublicKey publicKeyRcpt = new PublicKey(PUBLIC_KEY);

			ITag pkeyRcptTag = PubKeyTag.builder().publicKey(publicKeyRcpt).petName("willy").build();
			TagList tagList = new TagList();
			tagList.add(pkeyRcptTag);

			var event2 = new DirectMessageEvent(publicKeySender, tagList, "Hello Willy!");

			identity.encryptDirectMessage(event2);
			identity.sign(event2);

			GenericMessage message = new EventMessage(event2);

			client.send(message);

		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void mentionsEvent() throws NostrException {
		logHeader("mentionsEvent");

		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			ITag pkeySenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			TagList tagList = new TagList();
			tagList.add(pkeySenderTag);

			PubKeyTagList mentionees = new PubKeyTagList();
			PublicKey pk = new PublicKey(PUBLIC_KEY);
			mentionees.add(PubKeyTag.builder().publicKey(pk).build());

			GenericEvent event = new MentionsEvent(publicKeySender, tagList, "Hello " + PUBLIC_KEY, mentionees);
			identity.sign(event);

			log.log(Level.FINER, ">>>>>>>>>>>> Event: {0}", event);

			GenericMessage message = new EventMessage(event);

			client.send(message);

		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void deletionEvent() throws NostrException {
		logHeader("deletionEvent");

		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			TagList tagList = new TagList();
			tagList.add(pkSenderTag);

			GenericEvent event = new TextNoteEvent(publicKeySender, tagList, "Hello Astral, Please delete me!");

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);

			tagList = new TagList();
			tagList.add(EventTag.builder().idEvent(event.getId()).build());
			GenericEvent delEvent = new DeletionEvent(publicKeySender, tagList);

			identity.sign(delEvent);
			message = new EventMessage(delEvent);

			client.send(message);

		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void metaDataEvent() throws NostrException {
		logHeader("metaDataEvent");

		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			TagList tagList = new TagList();
			ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			tagList.add(pkSenderTag);

			var event = new MetadataEvent(publicKeySender, tagList, profile);

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);

		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void ephemerealEvent() throws NostrException {
		logHeader("ephemerealEvent");

		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			TagList tagList = new TagList();
			tagList.add(pkSenderTag);

			GenericEvent event = new EphemeralEvent(publicKeySender, tagList);

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);
		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void reactionEvent() throws NostrException {
		logHeader("reactionEvent");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			TagList tagList = new TagList();
			tagList.add(pkSenderTag);

			GenericEvent event = new TextNoteEvent(publicKeySender, tagList, "Hello Astral, Please like me!");

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);

			tagList = new TagList();
			tagList.add(EventTag.builder().idEvent(event.getId()).build());
			tagList.add(PubKeyTag.builder().publicKey(publicKeySender).build());
			GenericEvent reactionEvent = new ReactionEvent(publicKeySender, tagList, Reaction.LIKE, event);

			identity.sign(reactionEvent);
			message = new EventMessage(reactionEvent);

			client.send(message);

		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void replaceableEvent() throws NostrException {
		logHeader("replaceableEvent");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			TagList tagList = new TagList();
			tagList.add(pkSenderTag);

			GenericEvent event = new TextNoteEvent(publicKeySender, tagList, "Hello Astral, Please replace me!");

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);

			tagList = new TagList();
			tagList.add(EventTag.builder().idEvent(event.getId()).build());
			GenericEvent replaceableEvent = new ReplaceableEvent(publicKeySender, tagList, "New content", event);

			identity.sign(replaceableEvent);
			message = new EventMessage(replaceableEvent);

			client.send(message);

		} catch (UnsupportedNIPException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	private static void internetIdMetadata() throws NostrException {
		logHeader("internetIdMetadata");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
			TagList tagList = new TagList();
			tagList.add(pkSenderTag);

			GenericEvent event = new InternetIdentifierMetadataEvent(publicKeySender, tagList, identity.getProfile());

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);

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
			GenericMessage message = new ReqMessage(subId, filters);

			client.send(message);
		} catch (Exception ex) {
			throw new NostrException(ex);
		}
	}

	private static GenericEvent createChannel() throws NostrException {
		logHeader("createChannel");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			var channel = Channel.builder().name("JNostr Channel")
					.about("This is a channel to test NIP28 in nostr-java")
					.picture("https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg").build();
			GenericEvent event = new ChannelCreateEvent(publicKeySender, new TagList(), channel.toString());

			identity.sign(event);
			GenericMessage message = new EventMessage(event);

			client.send(message);

			return event;
		} catch (Exception ex) {
			throw new NostrException(ex);
		}
	}

	private static void updateChannelMetadata() throws NostrException {
		logHeader("updateChannelMetadata");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			var channelCreateEvent = createChannel();

			var tagList = new TagList();
			tagList.add(EventTag.builder().idEvent(channelCreateEvent.getId())
					.recommendedRelayUrl(client.getRelays().stream().findFirst().get().getUri()).build());

			var channel = Channel.builder().name("test change name")
					.about("This is a channel to test NIP28 in nostr-java | changed")
					.picture("https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg").build();
			GenericEvent event = new ChannelMetadataEvent(publicKeySender, tagList, channel.toString());

			identity.sign(event);
			var message = new EventMessage(event);

			client.send(message);
		} catch (Exception ex) {
			throw new NostrException(ex);
		}
	}

	private static GenericEvent sendChannelMessage() throws NostrException {
		logHeader("sendChannelMessage");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			var channelCreateEvent = createChannel();

			var tagList = new TagList();
			tagList.add(EventTag.builder().idEvent(channelCreateEvent.getId())
					.recommendedRelayUrl(client.getRelays().stream().findFirst().get().getUri()).marker(Marker.ROOT)
					.build());

			GenericEvent event = new ChannelMessageEvent(publicKeySender, tagList, "Hello everybody!");

			identity.sign(event);
			var message = new EventMessage(event);

			client.send(message);

			return event;
		} catch (Exception ex) {
			throw new NostrException(ex);
		}
	}

	private static GenericEvent hideMessage() throws NostrException {
		logHeader("hideMessage");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			var channelMessageEvent = sendChannelMessage();

			var tagList = new TagList();
			tagList.add(EventTag.builder().idEvent(channelMessageEvent.getId()).build());

			GenericEvent event = new HideMessageEvent(publicKeySender, tagList,
					ContentReason.builder().reason("Dick pic").build().toString());

			identity.sign(event);
			var message = new EventMessage(event);

			client.send(message);

			return event;
		} catch (Exception ex) {
			throw new NostrException(ex);
		}
	}

	private static GenericEvent muteUser() throws NostrException {
		logHeader("muteUser");
		try {
			final PublicKey publicKeySender = identity.getProfile().getPublicKey();

			var tagList = new TagList();
			tagList.add(PubKeyTag.builder().publicKey(new PublicKey(PUBLIC_KEY)).build());

			GenericEvent event = new MuteUserEvent(publicKeySender, tagList,
					ContentReason.builder().reason("Posting dick pics").build().toString());

			identity.sign(event);
			var message = new EventMessage(event);

			client.send(message);

			return event;
		} catch (Exception ex) {
			throw new NostrException(ex);
		}
	}

//    public static void sensitiveContentNote(Identity wallet, Client client) throws NostrException {
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
