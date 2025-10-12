package nostr.examples;

import nostr.api.NIP01;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.id.Identity;

import java.util.List;
import java.util.Map;

/** Demonstrates requesting events from a relay using filters for author and kind. */
public class FilterExample {

  private static final String RELAY_URL = "wss://relay.damus.io";

  public static void main(String[] args) throws Exception {
    var author = new PublicKey("21ef0d8541375ae4bca85285097fba370f7e540b5a30e5e75670c16679f9d144");

    var filters = new Filters(new AuthorFilter<>(author), new KindFilter<>(Kind.TEXT_NOTE));

    var subId = "filter-example-" + System.currentTimeMillis();

    Identity sender = Identity.generateRandomIdentity();
    NIP01 client = new NIP01(sender);
    client.setRelays(Map.of("damus", RELAY_URL));

    List<String> responses = client.sendRequest(filters, subId);

    var decoder = new BaseMessageDecoder<BaseMessage>();
    for (String json : responses) {
      BaseMessage message = decoder.decode(json);
      if (message instanceof EventMessage eventMessage) {
        System.out.println(eventMessage.getEvent());
      }
    }
    client.close();
  }
}
