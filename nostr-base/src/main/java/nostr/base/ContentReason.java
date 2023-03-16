package nostr.base;

import com.google.gson.Gson;

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
    	Gson gson = new Gson();
    	return gson.toJson(this).replace("\"", "\\\"");
    }

}
