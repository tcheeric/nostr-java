package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nostr.base.Encoder;
import nostr.event.BaseTag;
import nostr.event.json.EventJsonMapper;
import nostr.event.json.serializer.BaseTagSerializer;

public record BaseTagEncoder(BaseTag tag) implements Encoder {
  public static final ObjectMapper BASETAG_ENCODER_MAPPER_BLACKBIRD =
      EventJsonMapper.getMapper()
          .copy()
          .registerModule(new SimpleModule().addSerializer(new BaseTagSerializer()));

  @Override
  public String encode() throws nostr.event.json.codec.EventEncodingException {
    try {
      return BASETAG_ENCODER_MAPPER_BLACKBIRD.writeValueAsString(tag);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode tag", e);
    }
  }
}
