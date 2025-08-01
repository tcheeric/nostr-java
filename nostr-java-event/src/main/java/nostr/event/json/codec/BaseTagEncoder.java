package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nostr.base.Encoder;
import nostr.event.BaseTag;
import nostr.event.json.serializer.BaseTagSerializer;

public record BaseTagEncoder(BaseTag tag) implements Encoder {
    public static final ObjectMapper BASETAG_ENCODER_MAPPED_AFTERBURNER =
        ENCODER_MAPPED_AFTERBURNER.copy()
            .registerModule(
                new SimpleModule().addSerializer(
                    new BaseTagSerializer<>()));

    @Override
    public String encode() {
        try {
            return BASETAG_ENCODER_MAPPED_AFTERBURNER.writeValueAsString(tag);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
