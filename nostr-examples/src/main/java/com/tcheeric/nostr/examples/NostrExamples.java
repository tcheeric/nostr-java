package com.tcheeric.nostr.examples;

import com.tcheeric.nostr.base.ITag;
import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.NostrUtil;
import com.tcheeric.nostr.base.Profile;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.base.UnsupportedNIPException;
import com.tcheeric.nostr.event.BaseMessage;
import com.tcheeric.nostr.event.Kind;
import com.tcheeric.nostr.event.Reaction;
import com.tcheeric.nostr.event.impl.DeletionEvent;
import com.tcheeric.nostr.event.impl.DirectMessageEvent;
import com.tcheeric.nostr.event.impl.EphemeralEvent;
import com.tcheeric.nostr.event.impl.Filters;
import com.tcheeric.nostr.event.impl.GenericEvent;
import com.tcheeric.nostr.event.impl.InternetIdentifierMetadataEvent;
import com.tcheeric.nostr.event.impl.MentionsEvent;
import com.tcheeric.nostr.event.impl.MetadataEvent;
import com.tcheeric.nostr.event.impl.ReactionEvent;
import com.tcheeric.nostr.event.impl.ReplaceableEvent;
import com.tcheeric.nostr.event.impl.TextNoteEvent;
import com.tcheeric.nostr.event.list.KindList;
import com.tcheeric.nostr.event.list.PubKeyTagList;
import com.tcheeric.nostr.event.list.TagList;
import com.tcheeric.nostr.event.marshaller.impl.EventMarshaller;
import com.tcheeric.nostr.event.message.EventMessage;
import com.tcheeric.nostr.event.message.ReqMessage;
import com.tcheeric.nostr.event.tag.EventTag;
import com.tcheeric.nostr.event.tag.PubKeyTag;
import com.tcheeric.nostr.id.Client;
import com.tcheeric.nostr.id.Wallet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.LogManager;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class NostrExamples {

    static {
        final LogManager logManager = LogManager.getLogManager();
        try (final InputStream is = NostrExamples.class.getResourceAsStream("/logging.properties")) {
            logManager.readConfiguration(is);
        } catch (IOException ex) {
            System.exit(-1000);
        }
    }

    public static void main(String[] args) throws IOException, Exception {
        try {

            log.log(Level.INFO, "================= The Beginning");

            Wallet wallet = new Wallet();
            Client client = new Client("nostr-java", wallet);

            sendTextNoteEvent(wallet, client);

            Thread.sleep(500);

            try {
                sendEncryptedDirectMessage(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

            Thread.sleep(500);

            try {
                mentionsEvent(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

            Thread.sleep(500);

            try {
                deletionEvent(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

            Thread.sleep(500);

            try {
                metaDataEvent(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

            Thread.sleep(500);

            try {
                ephemerealEvent(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

            Thread.sleep(500);

            try {
                reactionEvent(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

            Thread.sleep(500);

            try {
                replaceableEvent(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

//            Thread.sleep(500);
//
//            try {
//                internetIdMetadata(wallet, client);
//            } catch (UnsupportedNIPException ex) {
//                log.log(Level.WARNING, ex.getMessage());
//            }

            Thread.sleep(500);

            try {
                filters(wallet, client);
            } catch (UnsupportedNIPException ex) {
                log.log(Level.WARNING, ex.getMessage());
            }

            log.log(Level.INFO, "================== The End");

        } catch (IllegalArgumentException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }
    }

    private static void sendTextNoteEvent(Wallet wallet, Client client) throws NostrException {
        logHeader("sendTextNoteEvent");
        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            TagList tagList = new TagList();
            tagList.add(pkSenderTag);

            GenericEvent event = new TextNoteEvent(publicKeySender, tagList, "Hello Astral, from the nostr-java API!");

            wallet.sign(event);
            BaseMessage message = EventMessage.builder().event(event).build();

            log.log(Level.FINE, "Sending message {0}", event);
            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void sendEncryptedDirectMessage(Wallet wallet, Client client) throws NostrException {
        logHeader("sendEncryptedDirectMessage");

        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();
            PublicKey publicKeyRcpt = new PublicKey(NostrUtil.hexToBytes("01739eae78ef308acb9e7a8a85f7d03484e0d338a7fae1ef2a8fa18e9b5915c5"));

            ITag pkeyRcptTag = PubKeyTag.builder().publicKey(publicKeyRcpt).petName("willy").build();
            TagList tagList = new TagList();
            tagList.add(pkeyRcptTag);

            GenericEvent event2 = new DirectMessageEvent(publicKeySender, tagList, "Hello Willy!");

            wallet.encryptDirectMessage((DirectMessageEvent) event2);
            wallet.sign(event2);

            BaseMessage message = EventMessage.builder().event(event2).build();

            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void mentionsEvent(Wallet wallet, Client client) throws NostrException {
        logHeader("mentionsEvent");

        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            ITag pkeySenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            TagList tagList = new TagList();
            tagList.add(pkeySenderTag);

            PubKeyTagList mentionees = new PubKeyTagList();
            PublicKey pk = new PublicKey(NostrUtil.hexToBytes("01739eae78ef308acb9e7a8a85f7d03484e0d338a7fae1ef2a8fa18e9b5915c5"));
            mentionees.add(PubKeyTag.builder().publicKey(pk).build());

            GenericEvent event = new MentionsEvent(publicKeySender, tagList, "Hello 01739eae78ef308acb9e7a8a85f7d03484e0d338a7fae1ef2a8fa18e9b5915c5", mentionees);
            wallet.sign(event);

            log.log(Level.INFO, ">>>>>>>>>>>> Event: {0}", event);

            BaseMessage message = EventMessage.builder().event(event).build();

            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void deletionEvent(Wallet wallet, Client client) throws NostrException {
        logHeader("deletionEvent");

        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            TagList tagList = new TagList();
            tagList.add(pkSenderTag);

            GenericEvent event = new TextNoteEvent(publicKeySender, tagList, "Hello Astral, Please delete me!");

            wallet.sign(event);
            BaseMessage message = EventMessage.builder().event(event).build();

            log.log(Level.FINE, "Sending message {0}", event);
            client.send(message);

            tagList = new TagList();
            tagList.add(EventTag.builder().relatedEvent(event).build());
            GenericEvent delEvent = new DeletionEvent(publicKeySender, tagList);

            wallet.sign(delEvent);
            message = EventMessage.builder().event(delEvent).build();

            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void metaDataEvent(Wallet wallet, Client client) throws NostrException {
        logHeader("metaDataEvent");

        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            TagList tagList = new TagList();
            ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            tagList.add(pkSenderTag);

            Profile profile = Profile.builder().about("He, this is me!").email("ecureuil@nostr.java").name("ecureuil").picture(new URL("https://britishwildlifecentre.co.uk/wp-content/uploads/2018/12/New-Website-Grey-Squirrel-12-18-1024x682.jpg")).publicKey(publicKeySender).build();

            GenericEvent event = new MetadataEvent(publicKeySender, tagList, profile);

            wallet.sign(event);
            BaseMessage message = EventMessage.builder().event(event).build();

            log.log(Level.FINE, "Sending message {0}", event);
            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void ephemerealEvent(Wallet wallet, Client client) throws NostrException {
        logHeader("ephemerealEvent");

        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            TagList tagList = new TagList();
            tagList.add(pkSenderTag);

            GenericEvent event = new EphemeralEvent(publicKeySender, tagList);

            wallet.sign(event);
            BaseMessage message = EventMessage.builder().event(event).build();

            log.log(Level.FINE, "Sending message {0}", event);
            client.send(message);
        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void reactionEvent(Wallet wallet, Client client) throws NostrException {
        logHeader("reactionEvent");
        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            TagList tagList = new TagList();
            tagList.add(pkSenderTag);

            GenericEvent event = new TextNoteEvent(publicKeySender, tagList, "Hello Astral, Please like me!");

            wallet.sign(event);
            BaseMessage message = EventMessage.builder().event(event).build();

            log.log(Level.FINE, "Sending message {0}", event);
            client.send(message);

            tagList = new TagList();
            tagList.add(EventTag.builder().relatedEvent(event).build());
            tagList.add(PubKeyTag.builder().publicKey(publicKeySender).build());
            GenericEvent reactionEvent = new ReactionEvent(publicKeySender, tagList, Reaction.LIKE, event);

            wallet.sign(reactionEvent);
            message = EventMessage.builder().event(reactionEvent).build();

            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void replaceableEvent(Wallet wallet, Client client) throws NostrException {
        logHeader("replaceableEvent");
        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            TagList tagList = new TagList();
            tagList.add(pkSenderTag);

            GenericEvent event = new TextNoteEvent(publicKeySender, tagList, "Hello Astral, Please replace me!");

            wallet.sign(event);
            BaseMessage message = EventMessage.builder().event(event).build();

            log.log(Level.FINE, "Sending message {0}", event);
            client.send(message);

            tagList = new TagList();
            tagList.add(EventTag.builder().relatedEvent(event).build());
            GenericEvent replaceableEvent = new ReplaceableEvent(publicKeySender, tagList, "New content", event);

            wallet.sign(replaceableEvent);
            message = EventMessage.builder().event(replaceableEvent).build();

            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    private static void internetIdMetadata(Wallet wallet, Client client) throws NostrException {
        logHeader("internetIdMetadata");
        try {
            final PublicKey publicKeySender = wallet.getProfile().getPublicKey();

            ITag pkSenderTag = PubKeyTag.builder().publicKey(publicKeySender).petName("nostr-java").build();
            TagList tagList = new TagList();
            tagList.add(pkSenderTag);

            GenericEvent event = new InternetIdentifierMetadataEvent(publicKeySender, tagList, wallet.getProfile());

            wallet.sign(event);
            BaseMessage message = EventMessage.builder().event(event).build();

            client.send(message);

        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
    }

    public static void filters(Wallet wallet, Client client) throws NostrException {
        logHeader("filters");
        try {
            KindList kindList = new KindList();
            kindList.add(Kind.EPHEMEREAL_EVENT);
            kindList.add(Kind.TEXT_NOTE);

            Filters filters = Filters.builder().kinds(kindList).limit(10).build();

            String subId = "subId" + System.currentTimeMillis();
            BaseMessage message = ReqMessage.builder().filters(filters).subscriptionId(subId).build();

            client.send(message);
        } catch (UnsupportedNIPException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NostrException(ex);
        }
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
}
