package nostr.examples;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import nostr.api.NIP01;
import nostr.api.NIP04;
import nostr.api.NIP05;
import nostr.api.NIP09;
import nostr.api.NIP25;
import nostr.api.NIP28;
import nostr.api.NIP30;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.entities.ChannelProfile;
import nostr.event.entities.Reaction;
import nostr.event.entities.UserProfile;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

/** Example demonstrating several nostr-java API calls. */
public class NostrApiExamples {

  private static final Identity RECIPIENT = Identity.generateRandomIdentity();
  private static final Identity SENDER = Identity.generateRandomIdentity();

  private static final UserProfile PROFILE =
      new UserProfile(SENDER.getPublicKey(), "Nostr Guy", "guy@nostr-java.io", "It's me!", null);
  private static final Map<String, String> RELAYS = Map.of("local", "localhost:5555");

  static {
    try {
      PROFILE.setPicture(
          new URI("https://images.unsplash.com/photo-1462888210965-cdf193fb74de").toURL());
    } catch (MalformedURLException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws Exception {
    new NostrApiExamples().run();
  }

  public void run() throws Exception {
    logAccountsData();

    metaDataEvent();
    sendTextNoteEvent();
    sendEncryptedDirectMessage();
    deletionEvent();
    ephemerealEvent();
    reactionEvent();
    replaceableEvent();
    internetIdMetadata();
    filters();
    createChannel();
    updateChannelMetadata();
    sendChannelMessage();
    hideMessage();
    muteUser();
  }

  private static GenericEvent sendTextNoteEvent() {
    logHeader("sendTextNoteEvent");

    List<BaseTag> tags = new ArrayList<>(List.of(new PubKeyTag(RECIPIENT.getPublicKey())));

    var nip01 = new NIP01(SENDER);
    nip01.createTextNoteEvent(tags, "Hello world, I'm here on nostr-java API!").sign().send(RELAYS);

    return nip01.getEvent();
  }

  private static void sendEncryptedDirectMessage() {
    logHeader("sendEncryptedDirectMessage");

    var nip04 = new NIP04(SENDER, RECIPIENT.getPublicKey());
    nip04.createDirectMessageEvent("Hello Nakamoto!").sign().send(RELAYS);
  }

  private static void deletionEvent() {
    logHeader("deletionEvent");

    var event = sendTextNoteEvent();

    var nip09 = new NIP09(SENDER);
    nip09.createDeletionEvent(event).sign().send();
  }

  private static GenericEvent metaDataEvent() {
    logHeader("metaDataEvent");

    var nip01 = new NIP01(SENDER);
    nip01.createMetadataEvent(PROFILE).sign().send(RELAYS);

    return nip01.getEvent();
  }

  private static void ephemerealEvent() {
    logHeader("ephemeralEvent");

    var nip01 = new NIP01(SENDER);
    nip01
        .createEphemeralEvent(Kind.EPHEMEREAL_EVENT.getValue(), "An ephemeral event")
        .sign()
        .send(RELAYS);
  }

  private static void reactionEvent() {
    logHeader("reactionEvent");

    List<BaseTag> tags =
        new ArrayList<>(
            List.of(
                NIP30.createEmojiTag(
                    "soapbox", "https://gleasonator.com/emoji/Gleasonator/soapbox.png")));
    var nip01 = new NIP01(SENDER);
    var event = nip01.createTextNoteEvent(tags, "Hello Astral, Please like me! :soapbox:");
    event.signAndSend(RELAYS);

    var nip25 = new NIP25(RECIPIENT);
    var reactionEvent =
        nip25.createReactionEvent(event.getEvent(), Reaction.LIKE, new Relay("localhost:5555"));
    reactionEvent.signAndSend(RELAYS);
    nip25
        .createReactionEvent(event.getEvent(), "\uD83D\uDCA9", new Relay("localhost:5555"))
        .signAndSend();

    BaseTag eventTag = NIP01.createEventTag(event.getEvent().getId());
    nip25
        .createReactionEvent(
            eventTag,
            NIP30.createEmojiTag(
                "ablobcatrainbow", "https://gleasonator.com/emoji/blobcat/ablobcatrainbow.png"))
        .signAndSend();
  }

  private static void replaceableEvent() {
    logHeader("replaceableEvent");

    var nip01 = new NIP01(SENDER);
    var event = nip01.createTextNoteEvent("Hello Astral, Please replace me!");
    event.signAndSend(RELAYS);

    nip01
        .createReplaceableEvent(
            List.of(NIP01.createEventTag(event.getEvent().getId())),
            Kind.REPLACEABLE_EVENT.getValue(),
            "New content")
        .signAndSend();
  }

  private static void internetIdMetadata() {
    logHeader("internetIdMetadata");
    var profile =
        UserProfile.builder()
            .name("Guilherme Gps")
            .publicKey(
                new PublicKey("21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144"))
            .nip05("me@guilhermegps.com.br")
            .build();

    var nip05 = new NIP05(SENDER);
    nip05.createInternetIdentifierMetadataEvent(profile).sign().send(RELAYS);
  }

  private static void filters() throws InterruptedException {
    logHeader("filters");

    var date = Calendar.getInstance();
    var subId = "subId" + date.getTimeInMillis();
    date.add(Calendar.DAY_OF_MONTH, -5);

    var nip01 = NIP01.getInstance();
    nip01
        .setRelays(RELAYS)
        .sendRequest(
            new Filters(
                new KindFilter<>(Kind.EPHEMEREAL_EVENT),
                new KindFilter<>(Kind.TEXT_NOTE),
                new AuthorFilter<>(
                    new PublicKey(
                        "21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144")),
                new SinceFilter(date.getTimeInMillis() / 1000)),
            subId);

    Thread.sleep(5000);
  }

  private static GenericEvent createChannel() {
    try {
      logHeader("createChannel");

      var channel =
          new ChannelProfile(
              "JNostr Channel",
              "This is a channel to test NIP28 in nostr-java",
              "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg");
      var nip28 = new NIP28(SENDER);
      nip28.setSender(SENDER);
      nip28.createChannelCreateEvent(channel).sign().send();
      return nip28.getEvent();
    } catch (MalformedURLException | URISyntaxException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static void updateChannelMetadata() {
    try {
      logHeader("updateChannelMetadata");

      var channelCreateEvent = createChannel();
      var channel =
          new ChannelProfile(
              "test change name",
              "This is a channel to test NIP28 in nostr-java | changed",
              "https://cdn.pixabay.com/photo/2020/05/19/13/48/cartoon-5190942_960_720.jpg");

      var nip28 = new NIP28(SENDER);
      nip28.updateChannelMetadataEvent(channelCreateEvent, channel, null).sign().send();

    } catch (MalformedURLException | URISyntaxException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static GenericEvent sendChannelMessage() {
    logHeader("sendChannelMessage");

    var channelCreateEvent = createChannel();

    var nip28 = new NIP28(SENDER);
    nip28
        .createChannelMessageEvent(
            channelCreateEvent, new Relay("localhost:5555"), "Hello everybody!")
        .sign()
        .send();

    return nip28.getEvent();
  }

  private static GenericEvent hideMessage() {
    logHeader("hideMessage");

    var channelMessageEvent = sendChannelMessage();

    var nip28 = new NIP28(SENDER);
    nip28.createHideMessageEvent(channelMessageEvent, "Dick pic").sign().send();

    return nip28.getEvent();
  }

  private static GenericEvent muteUser() {
    logHeader("muteUser");

    var nip28 = new NIP28(SENDER);
    nip28.createMuteUserEvent(RECIPIENT.getPublicKey(), "Posting dick pics").sign().send();

    return nip28.getEvent();
  }

  private static void logAccountsData() {
    String msg =
        "################################ ACCOUNTS BEGINNING ################################"
            + '\n'
            + "*** RECEIVER ***"
            + '\n'
            + '\n'
            + "* PrivateKey: "
            + RECIPIENT.getPrivateKey().toBech32String()
            + '\n'
            + "* PrivateKey HEX: "
            + RECIPIENT.getPrivateKey().toString()
            + '\n'
            + "* PublicKey: "
            + RECIPIENT.getPublicKey().toBech32String()
            + '\n'
            + "* PublicKey HEX: "
            + RECIPIENT.getPublicKey().toString()
            + '\n'
            + '\n'
            + "*** SENDER ***"
            + '\n'
            + '\n'
            + "* PrivateKey: "
            + SENDER.getPrivateKey().toBech32String()
            + '\n'
            + "* PrivateKey HEX: "
            + SENDER.getPrivateKey().toString()
            + '\n'
            + "* PublicKey: "
            + SENDER.getPublicKey().toBech32String()
            + '\n'
            + "* PublicKey HEX: "
            + SENDER.getPublicKey().toString()
            + '\n'
            + '\n'
            + "################################ ACCOUNTS END ################################";

    System.out.println(msg);
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
