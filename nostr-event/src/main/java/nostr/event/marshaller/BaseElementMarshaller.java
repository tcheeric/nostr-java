package nostr.event.marshaller;

import java.lang.reflect.Field;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IElement;
import nostr.base.IMarshaller;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
public abstract class BaseElementMarshaller implements IMarshaller {

    private final IElement element;
    private final Relay relay;
    private final ObjectMapper mapper;
    private boolean escape;

    public BaseElementMarshaller(IElement element, Relay relay) {
        this(element, relay, false);
    }

    public BaseElementMarshaller(IElement element, Relay relay, boolean escape) {
        this(element, relay, new ObjectMapper(), escape);
        
		mapper.setSerializationInclusion(Include.NON_NULL);
    }

    protected boolean nipFieldSupport(Field field) {
        
        if (relay == null) {
            return true;
        }

        return NipUtil.checkSupport(relay, field);
    }
    
    protected String toJson() throws NostrException {
    	try {
	    	return mapper.writeValueAsString(element);
		} catch (Exception e) {
			throw new NostrException(e);
		} 
    }
}
