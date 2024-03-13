package nostr.ws.handler.spi;

import java.util.List;

import lombok.NonNull;
import nostr.base.IHandler;
import nostr.base.Relay;
import nostr.event.BaseMessage;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IResponseHandler extends IHandler {
    
	List<BaseMessage> getResponses();
	
	void setResponses(@NonNull List<BaseMessage> responses);
	
    void process(@NonNull String message, Relay relay) throws NostrException;

}
