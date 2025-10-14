package nostr.api.unit;

import nostr.api.NIP01;
import nostr.base.Kind;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for NIP-01 message creation and encoding. */
public class NIP01MessagesTest {

  @Test
  // EVENT message encodes with command and optional subscription id
  void eventMessageEncodes() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    NIP01 nip01 = new NIP01(sender);
    GenericEvent event = nip01.createTextNoteEvent("hi").sign().getEvent();

    EventMessage msg = NIP01.createEventMessage(event, "sub-ev");
    String json = msg.encode();
    assertTrue(json.contains("\"EVENT\""));
    assertTrue(json.contains("\"sub-ev\""));
  }

  @Test
  // REQ message encodes subscription id and filters
  void reqMessageEncodes() throws Exception {
    Filters filters = new Filters(new KindFilter<>(Kind.TEXT_NOTE));
    ReqMessage msg = NIP01.createReqMessage("sub-req", List.of(filters));
    String json = msg.encode();
    assertTrue(json.contains("\"REQ\""));
    assertTrue(json.contains("\"sub-req\""));
    assertTrue(json.contains("\"kinds\""));
  }

  @Test
  // CLOSE message encodes subscription id
  void closeMessageEncodes() throws Exception {
    CloseMessage msg = NIP01.createCloseMessage("sub-close");
    String json = msg.encode();
    assertTrue(json.contains("\"CLOSE\""));
    assertTrue(json.contains("\"sub-close\""));
  }

  @Test
  // EOSE message encodes subscription id
  void eoseMessageEncodes() throws Exception {
    EoseMessage msg = NIP01.createEoseMessage("sub-eose");
    String json = msg.encode();
    assertTrue(json.contains("\"EOSE\""));
    assertTrue(json.contains("\"sub-eose\""));
  }

  @Test
  // NOTICE message encodes human readable message
  void noticeMessageEncodes() throws Exception {
    NoticeMessage msg = NIP01.createNoticeMessage("hello");
    String json = msg.encode();
    assertTrue(json.contains("\"NOTICE\""));
    assertTrue(json.contains("\"hello\""));
  }
}

