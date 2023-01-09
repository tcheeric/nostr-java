package nostr.event.unmarshaller.impl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nostr.base.GenericTagQuery;
import nostr.base.IElement;
import nostr.base.PublicKey;
import nostr.event.BaseEvent.ProxyEvent;
import nostr.event.Kind;
import nostr.event.impl.Filters;
import nostr.event.list.EventList;
import nostr.event.list.GenericTagQueryList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import nostr.event.unmarshaller.BaseElementUnmarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.types.values.impl.ArrayValue;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.NumberValue;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
public class FiltersUnmarshaller extends BaseElementUnmarshaller {

    public FiltersUnmarshaller(String event) {
        this(event, false);
    }

    public FiltersUnmarshaller(String event, boolean escape) {
        super(event, escape);
    }

    @Override
    public IElement unmarshall() throws NostrException {

        var value = new JsonObjectUnmarshaller(getJson()).unmarshall();
        var publicKeyList = new PublicKeyList();
        var refPublicKeyList = new PublicKeyList();
        var events = new EventList();
        var referencedEvents = new EventList();
        var kindList = new KindList();
        var genericTagQueryList = getGenericTagQueryList();
        ArrayValue arr;
        // Authors
        var optArr = value.get("\"authors\"");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                String hex = arr.get(i).get().getValue().toString();
                publicKeyList.add(new PublicKey(NostrUtil.hexToBytes(hex)));
            }
        }

        // Ref. pub keys
        optArr = value.get("\"#p\"");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                String hex = arr.get(i).get().getValue().toString();
                refPublicKeyList.add(new PublicKey(NostrUtil.hexToBytes(hex)));
            }
        }

        //  Events        
        optArr = value.get("\"ids\"");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                String eventId = arr.get(i).get().getValue().toString();
                try {
                    events.add(new ProxyEvent(eventId));
                } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
                    throw new NostrException(ex);
                }
            }
        }

        // Referenced Events        
        optArr = value.get("\"#e\"");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            referencedEvents = new EventList();
            for (var i = 0; i < arr.length(); i++) {
                String eventId = arr.get(i).get().getValue().toString();
                try {
                    referencedEvents.add(new ProxyEvent(eventId));
                } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
                    throw new NostrException(ex);
                }
            }
        }

        // Kinds
        optArr = value.get("\"kinds\"");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                var ikind = ((NumberValue) arr.get(i).get()).intValue().get();
                Kind kind = Kind.valueOf(ikind);
                kindList.add(kind);
            }
        }

        //Limit
        var optNumber = value.get("\"limit\"");
        Integer limit = optNumber.isEmpty() ? null : ((NumberValue) optNumber.get()).intValue().get();

        // Since
        optNumber = value.get("\"since\"");
        var since = optNumber.isEmpty() ? null : ((NumberValue) optNumber.get()).longValue().get();

        // Since
        optNumber = value.get("\"until\"");
        var until = optNumber.isEmpty() ? null : ((NumberValue) optNumber.get()).longValue().get();

        // Generic TagList 
        Filters filters = Filters.builder()
                .authors(publicKeyList)
                .events(events)
                .kinds(kindList).limit(limit)
                .since(since)
                .referencedEvents(referencedEvents)
                .referencePubKeys(publicKeyList)
                .genericTagQueryList(genericTagQueryList)
                .until(until)
                .build();

        return filters;
    }

    private GenericTagQueryList getGenericTagQueryList() {
        GenericTagQueryList result = new GenericTagQueryList();
        var obj = new JsonObjectUnmarshaller(getJson()).unmarshall();
        var exprList = (List<ExpressionValue>) obj.getValue();

        for (var e : exprList) {
            if (!Arrays.asList("\"authors\"", "\"ids\"", "\"since\"", "\"until\"", "\"limit\"", "\"kinds\"", "\"#e\"", "\"sids\"", "\"#p\"").contains(e.getName())) {
                var tagName = e.getName().charAt(2);
                var valueList = new ArrayList<String>();

                final ArrayValue attrArr = (ArrayValue) e.getValue();
                for (var i = 0; i < attrArr.length(); i++) {
                    var s = attrArr.get(i).get().getValue().toString();
                    valueList.add(s);
                }
                var gtq = GenericTagQuery.builder().tagName(tagName).value(valueList).build();
                result.add(gtq);
            }
        }

        return result.size() == 0 ? null : result;
    }

}
