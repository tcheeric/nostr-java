package nostr.event.unmarshaller.impl;

import nostr.base.Command;
import nostr.base.IEvent;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.list.FiltersList;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import nostr.event.unmarshaller.BaseElementUnmarshaller;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class MessageUnmarshaller extends BaseElementUnmarshaller {

    public MessageUnmarshaller(String event) {
        this(event, false);
    }

    public MessageUnmarshaller(String event, boolean escape) {
        super(event, escape);
    }

    @Override
    public BaseMessage unmarshall() {

        var value = new JsonArrayUnmarshaller(this.getJson()).unmarshall();

        Command command = Command.valueOf(value.get(0).get().getValue().toString());

        switch (command) {
            case CLOSE -> {
                String subId = value.get(1).get().getValue().toString();
                return CloseMessage.builder().subscriptionId(subId).build();
            }
            case EVENT -> {
                IEvent event = new EventUnmarshaller(value.get(1).get().getValue().toString()).unmarshall();
                return EventMessage.builder().event((GenericEvent) event).build();
            }
            case EOSE -> {
                String subId = value.get(1).get().getValue().toString();
                return EoseMessage.builder().subscriptionId(subId).build();
            }
            case NOTICE -> {
                String message = value.get(1).get().getValue().toString();
                return NoticeMessage.builder().message(message).build();
            }
            case OK -> {
                String eventId = value.get(1).get().getValue().toString();
                Boolean flag = (Boolean) value.get(2).get().getValue();
                String message = value.get(3).get().getValue().toString();
                return new OkMessage(eventId, flag, message);
            }
            case REQ -> {
                String subId = value.get(1).get().getValue().toString();
                FiltersList filtersList = new FiltersList();
                for (var i = 1; i < value.length(); i++) {
                    var filters = value.get(i).get();
                    filtersList.add((Filters) new FiltersUnmarshaller(filters.toString(), isEscape()).unmarshall());

                }
                return new ReqMessage(subId, filtersList);
            }
            default ->
                throw new RuntimeException("Invalid command " + command);
        }
    }

}
