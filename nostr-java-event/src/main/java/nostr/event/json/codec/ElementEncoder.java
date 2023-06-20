package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.IElement;
import nostr.base.IEncoder;
import static nostr.base.IEncoder.MAPPER;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Log
@Data
public class ElementEncoder implements IEncoder {

    private final IElement element;
    private final Relay relay;

    public ElementEncoder(IElement element) {
        this(element, null);
    }

    @Override
    public String encode() throws UnsupportedNIPException, NostrException {
        if (!nipEventSupport()) {
            throw new UnsupportedNIPException("NIP is not supported by relay: \"" + relay.getName() + "\"  - List of supported NIP(s): " + relay.printSupportedNips());
        }

        return toJson();
    }

    protected boolean nipFieldSupport(Field field) {
        return (relay != null) ? NipUtil.checkSupport(relay, field) : true;
    }

    protected String toJson() throws NostrException {
        try {
            return MAPPER.writeValueAsString(element);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }

    private boolean nipEventSupport() {
        return (relay != null) ? NipUtil.checkSupport(relay, element) : true;
    }

}
