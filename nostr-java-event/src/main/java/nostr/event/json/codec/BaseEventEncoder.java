package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.IEncoder;
import static nostr.base.IEncoder.MAPPER;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.event.BaseEvent;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Log
@Data
public class BaseEventEncoder implements IEncoder<BaseEvent> {

    private final BaseEvent event;
    private final Relay relay;

    public BaseEventEncoder(BaseEvent event) {
        this(event, null);
    }

    @Override
    public String encode() {
        if (!nipEventSupport()) {
            try {
                throw new UnsupportedNIPException("NIP is not supported by relay: \"" + relay.getName() + "\"  - List of supported NIP(s): " + relay.printSupportedNips());
            } catch (UnsupportedNIPException ex) {
                throw new RuntimeException(ex);
            }
        }

        try {
            return toJson();
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected boolean nipFieldSupport(Field field) {
        return relay == null || NipUtil.checkSupport(relay, field);
    }

    protected String toJson() throws NostrException {
        try {
            return MAPPER.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }

    private boolean nipEventSupport() {
        return relay == null || NipUtil.checkSupport(relay, event);
    }

}
