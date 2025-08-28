package nostr.event.json.codec;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import nostr.event.BaseEvent;
import org.junit.jupiter.api.Test;

class BaseEventEncoderTest {

  static class FailingSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      throw new IOException("Serialization failure");
    }
  }

  static class FailingEvent extends BaseEvent {
    @JsonSerialize(using = FailingSerializer.class)
    public String getAttr() {
      return "boom";
    }

    @Override
    public String getId() {
      return "";
    }

    @Override
    public String toBech32() {
      return "";
    }
  }

  @Test
  // Ensures encode throws EventEncodingException when serialization fails
  void encodeThrowsEventEncodingException() {
    var encoder = new BaseEventEncoder<>(new FailingEvent());
    assertThrows(EventEncodingException.class, encoder::encode);
  }
}
