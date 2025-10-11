package nostr.event.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import nostr.base.PublicKey;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.BaseTag;
import org.junit.jupiter.api.Test;

@Slf4j
public class BaseMessageCommandMapperTest {
  //  TODO: flesh out remaining commands
  public static final String REQ_JSON =
      "[\"REQ\", "
          + "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", "
          + "{\"kinds\": [1], "
          + "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"],"
          + "\"#e\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

  @Test
  public void testReqMessageDecoder() throws JsonProcessingException {

    BaseMessage decode = new BaseMessageDecoder<>().decode(REQ_JSON);
    assertInstanceOf(ReqMessage.class, decode);
  }

  @Test
  public void testReqMessageDecoderType() {

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
  public void testReqMessageDecoderThrows() {

    assertThrows(
        ClassCastException.class,
        () -> {
          EoseMessage decode = new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
        });
  }

  @Test
  public void testReqMessageDecoderDoesNotThrow() {

    assertDoesNotThrow(
        () -> {
          new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
        });
  }

  @Test
  public void testReqMessageDecoderThrows3() {

    assertThrows(
        ClassCastException.class,
        () -> {
          EoseMessage decode = new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
        });
  }

  @Test
  // Maps EVENT message JSON to EventMessage type using roundtrip encode/decode.
  public void testEventMessageTypeMapping() throws Exception {
    GenericEvent ev = new GenericEvent(new PublicKey("f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75"), 1, new ArrayList<BaseTag>(), "hi");
    String json = new EventMessage(ev, "sub-2").encode();
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(EventMessage.class, decoded);
  }

  @Test
  // Maps CLOSE message JSON to CloseMessage type.
  public void testCloseMessageTypeMapping() {
    String json = "[\"CLOSE\", \"sub-3\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(CloseMessage.class, decoded);
  }

  @Test
  // Maps EOSE message JSON to EoseMessage type.
  public void testEoseMessageTypeMapping() {
    String json = "[\"EOSE\", \"sub-4\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(EoseMessage.class, decoded);
  }

  @Test
  // Maps NOTICE message JSON to NoticeMessage type.
  public void testNoticeMessageTypeMapping() {
    String json = "[\"NOTICE\", \"hello\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(NoticeMessage.class, decoded);
  }

  @Test
  // Maps OK message JSON to OkMessage type.
  public void testOkMessageTypeMapping() {
    String json = "[\"OK\", \"eventid\", true, \"ok\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(OkMessage.class, decoded);
  }

  @Test
  // Maps AUTH relay challenge JSON to RelayAuthenticationMessage type.
  public void testAuthRelayTypeMapping() {
    String json = "[\"AUTH\", \"challenge\"]";
    BaseMessage decoded = new BaseMessageDecoder<>().decode(json);
    assertInstanceOf(RelayAuthenticationMessage.class, decoded);
  }
}
