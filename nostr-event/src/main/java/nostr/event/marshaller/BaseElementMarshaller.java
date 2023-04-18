package nostr.event.marshaller;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.IElement;
import nostr.base.IEvent;
import nostr.base.IMarshaller;
import nostr.base.INostrList;
import nostr.base.ITag;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericMessage;
import nostr.event.list.TagList;
import nostr.event.marshaller.impl.EventMarshaller;
import nostr.event.marshaller.impl.FiltersMarshaller;
import nostr.event.marshaller.impl.MessageMarshaller;
import nostr.event.marshaller.impl.TagListMarshaller;
import nostr.event.marshaller.impl.TagMarshaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
@Log
public abstract class BaseElementMarshaller implements IMarshaller {

    private final IElement element;
    private final Relay relay;
    private boolean escape;

    public BaseElementMarshaller(IElement element, Relay relay) {
        this(element, relay, false);
    }

    protected boolean nipFieldSupport(Field field) {
        
        if (relay == null) {
            return true;
        }

        return NipUtil.checkSupport(relay, field);
    }
    
    protected String toJson(ITag iTag) throws NostrException {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.setSerializationInclusion(Include.NON_NULL);
    	try {
	    	JsonNode node = mapper.valueToTree(iTag);
	    	
	    	Iterator<Entry<String,JsonNode>> fields = node.fields();
	    	
	    	var list = StreamSupport.stream(
	                Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED), false)
	                .map(f -> f.getValue().asText().toLowerCase() )
	                .collect(Collectors.toList());
	    	
	    	return mapper.valueToTree(list).toString();
		} catch (Exception e) {
			throw new NostrException(e);
		} 
    }

    @Builder
    @AllArgsConstructor
    @Data
    public static class Factory {

        private final IElement element;

        public IMarshaller create(Relay relay, boolean escape) throws NostrException {
            if (element instanceof IEvent iEvent) {
                return new EventMarshaller(iEvent, relay, escape);
            } else if (element instanceof ITag iTag) {
                return new TagMarshaller(iTag, relay, escape);
            } else if (element instanceof GenericMessage genericMessage) {
                return new MessageMarshaller(genericMessage, relay, escape);
            } else if (element instanceof TagList tagList) {
                return new TagListMarshaller(tagList, relay, escape);
            } else if (element instanceof INostrList iNostrList) {
                return new BaseListMarhsaller(iNostrList, relay, escape) {
                };
            } else if (element instanceof Filters filters) {
                return new FiltersMarshaller(filters, relay, escape);
            } else {
                throw new NostrException("Invalid Element type");
            }
        }
    }
}
