package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Data;
import lombok.NonNull;
import nostr.base.Encoder;
import nostr.event.BaseTag;
import nostr.event.json.serializer.TagSerializer;

@Data
public class BaseTagEncoder implements Encoder {
    public static final ObjectMapper BASETAGENCODER_MAPPED_AFTERBURNER =
        ENCODER_MAPPED_AFTERBURNER.copy()
            .registerModule(
                new SimpleModule().addSerializer(
                    new TagSerializer()));

    private final BaseTag tag;

    public BaseTagEncoder(@NonNull BaseTag tag) {
        this.tag = tag;
    }

    @Override
    public String encode() {
        try {
            String s = BASETAGENCODER_MAPPED_AFTERBURNER.writeValueAsString(tag);
            return s;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
