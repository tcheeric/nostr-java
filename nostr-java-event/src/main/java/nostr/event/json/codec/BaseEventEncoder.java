package nostr.event.json.codec;

import lombok.Data;
import lombok.SneakyThrows;
import nostr.base.Encoder;
import nostr.event.BaseEvent;

@Data
public class BaseEventEncoder<T extends BaseEvent> implements Encoder {

    private final T event;

    public BaseEventEncoder(T event) {
        this.event = event;
    }

    @Override
//    TODO: refactor all methods calling this to properly handle invalid json exception
    @SneakyThrows
    public String encode() {
        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(event);
    }
}
