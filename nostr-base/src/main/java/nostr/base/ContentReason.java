package nostr.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author guilhermegps
 *
 */
@Builder
@Data
@EqualsAndHashCode
public class ContentReason {

    private String reason;
    
    @Override
    public String toString() {
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			return mapper.writeValueAsString(this).replace("\"", "\\\"");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
    }

}
