package nostr.event.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class BaseMessageDecoderTest {
  //  TODO: flesh out remaining commands
  public static final String REQ_JSON =
      "[\"REQ\", "
          + "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", "
          + "{\"kinds\": [1], "
          + "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"],"
          + "\"#e\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

  public static final String INVALID_COMMAND_JSON =
      "[\"OTHER\", "
          + "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", "
          + "{\"kinds\": [1], "
          + "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"],"
          + "\"#e\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

  public static final String MALFORMED_JSON = "[\"REQ\"]";

  @Test
  void testReqMessageDecoder() throws JsonProcessingException {

    BaseMessage decode = new BaseMessageDecoder<>().decode(REQ_JSON);
    assertInstanceOf(ReqMessage.class, decode);
  }

  @Test
  void testReqMessageDecoderType() {

    assertDoesNotThrow(
        () -> {
          new BaseMessageDecoder<ReqMessage>().decode(REQ_JSON);
        });

    assertDoesNotThrow(
        () -> {
          ReqMessage reqMessage = new BaseMessageDecoder<ReqMessage>().decode(REQ_JSON);
        });
  }

  @Test
  void testReqMessageDecoderThrows() {

    assertThrows(
        ClassCastException.class,
        () -> {
          EoseMessage decode = new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
        });
  }

  @Test
  void testReqMessageDecoderDoesNotThrow() {

    assertDoesNotThrow(
        () -> {
          new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
        });
  }

  @Test
  void testReqMessageDecoderThrows3() {

    assertThrows(
        ClassCastException.class,
        () -> {
          EoseMessage decode = new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
        });
  }

  @Test
  void testInvalidMessageDecoder() {

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new BaseMessageDecoder<EoseMessage>().decode(INVALID_COMMAND_JSON);
        });
  }

  @Test
  void testMalformedJsonThrows() {

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new BaseMessageDecoder<>().decode(MALFORMED_JSON);
        });
  }

  @Test
  // Decodes an EVENT message without subscription id using the encoder/decoder roundtrip.
  void testEventMessageDecodeWithoutSubscription() throws Exception {
    GenericEvent ev = new GenericEvent(new PublicKey("f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75"), 1, new ArrayList<BaseTag>(), "hi");
    String json = new EventMessage(ev).encode();
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(EventMessage.class, decoded);
  }

  @Test
  // Decodes an EVENT message with subscription id using the encoder/decoder roundtrip.
  void testEventMessageDecodeWithSubscription() throws Exception {
    GenericEvent ev = new GenericEvent(new PublicKey("f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75"), 1, new ArrayList<BaseTag>(), "hi");
    String json = new EventMessage(ev, "sub-1").encode();
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(EventMessage.class, decoded);
  }

  @Test
  // Decodes a CLOSE message to the proper type.
  void testCloseMessageDecode() throws Exception {
    String json = "[\"CLOSE\", \"sub-1\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(CloseMessage.class, decoded);
  }

  @Test
  // Decodes an EOSE message to the proper type.
  void testEoseMessageDecode() throws Exception {
    String json = "[\"EOSE\", \"sub-1\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(EoseMessage.class, decoded);
  }

  @Test
  // Decodes a NOTICE message to the proper type.
  void testNoticeMessageDecode() throws Exception {
    String json = "[\"NOTICE\", \"hello\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(NoticeMessage.class, decoded);
  }

  @Test
  // Decodes an OK message to the proper type.
  void testOkMessageDecode() throws Exception {
    String json = "[\"OK\", \"eventid\", true, \"\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(OkMessage.class, decoded);
  }

  @Test
  // Decodes a relay AUTH challenge to the proper type.
  void testAuthRelayChallengeDecode() throws Exception {
    String json = "[\"AUTH\", \"challenge-string\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(RelayAuthenticationMessage.class, decoded);
  }

  //    @Test
  //    void assertionFail() {
  //        assertEquals(1, 2);
  //    }

  //    @Test
  //    void catastrophicTestFail() {
  //        log.info("makeSureIntegrationTestsFailhere");
  //
  //        assertDoesNotThrow(() -> {
  //            new BaseMessageDecoder<EoseMessage>().decode(INVALID_COMMAND_JSON);
  //        });
  //    }
}
