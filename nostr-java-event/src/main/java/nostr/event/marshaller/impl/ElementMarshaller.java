package nostr.event.marshaller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.IElement;
import nostr.base.IMarshaller;
import static nostr.base.IMarshaller.MAPPER;
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
public class ElementMarshaller implements IMarshaller {

    private final IElement element;
    private final Relay relay;

    @Override
    public String marshall() throws UnsupportedNIPException, NostrException {
        if (!nipEventSupport()) {
            throw new UnsupportedNIPException("NIP is not supported by relay: \"" + relay.getName() + "\"  - List of supported NIP(s): " + relay.printSupportedNips());
        }

        return toJson();
    }

    private boolean nipEventSupport() {
        return (relay != null) ? NipUtil.checkSupport(relay, getElement()) : true;
    }

    protected boolean nipFieldSupport(Field field) {
        return (relay != null) ? NipUtil.checkSupport(relay, field) : true;
    }

    private String toJson() throws NostrException {
        try {
            return MAPPER.writeValueAsString(element);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
