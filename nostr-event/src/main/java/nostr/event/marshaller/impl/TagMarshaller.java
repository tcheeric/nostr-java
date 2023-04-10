package nostr.event.marshaller.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.IMarshaller;
import nostr.base.ITag;
import nostr.base.Relay;
import nostr.event.serializer.CustomTagSerializer;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Data
@Builder
public class TagMarshaller implements IMarshaller {

    private final ITag tag;
    private final Relay relay;

    @Override
    public String marshall() throws NostrException {
        return toJson();
    }
    
    @Override
    public String toJson() throws NostrException {
    	try {
    		SimpleModule module = new SimpleModule();
    		module.addSerializer(new CustomTagSerializer());
    		var mappe = (new ObjectMapper())
    				.setSerializationInclusion(Include.NON_NULL)
    				.registerModule(module);
    		
	    	return mappe.writeValueAsString(tag);
		} catch (Exception e) {
			throw new NostrException(e);
		} 
    }

}
