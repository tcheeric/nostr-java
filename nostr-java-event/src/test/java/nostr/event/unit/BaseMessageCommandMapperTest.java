package nostr.event.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EoseMessage;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class BaseMessageCommandMapperTest {
//  TODO: flesh out remaining commands
  public final static String REQ_JSON =
      "[\"REQ\", " +
          "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", " +
          "{\"kinds\": [1], " +
          "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"]," +
          "\"#e\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

  @Test
  public void testReqMessageDecoder() throws JsonProcessingException {
    log.info("testReqMessageDecoder");

    BaseMessage decode = new BaseMessageDecoder<>().decode(REQ_JSON);
    assertInstanceOf(ReqMessage.class, decode);
  }

  @Test
  public void testReqMessageDecoderType() {
    log.info("testReqMessageDecoderType");

    assertDoesNotThrow(() -> {
      new BaseMessageDecoder<ReqMessage>().decode(REQ_JSON);
    });

    assertDoesNotThrow(() -> {
      ReqMessage reqMessage = new BaseMessageDecoder<ReqMessage>().decode(REQ_JSON);
    });
  }

  @Test
  public void testReqMessageDecoderThrows() {
    log.info("testReqMessageDecoderThrows");

    assertThrows(ClassCastException.class, () -> {
      EoseMessage decode = new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
    });
  }

  @Test
  public void testReqMessageDecoderDoesNotThrow() {
    log.info("testReqMessageDecoderDoesNotThrow");

    assertDoesNotThrow(() -> {
      new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
    });
  }

  @Test
  public void testReqMessageDecoderThrows3() {
    log.info("testReqMessageDecoderThrows");

    assertThrows(ClassCastException.class, () -> {
      EoseMessage decode = new BaseMessageDecoder<EoseMessage>().decode(REQ_JSON);
    });
  }
}
