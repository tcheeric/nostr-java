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

        // Authors
        var arr = (ArrayValue) value.get("\"authors\"");
        if (arr != null) {
            for (var i = 0; i < arr.length(); i++) {
                String hex = arr.get(i).getValue().toString();
                publicKeyList.add(new PublicKey(NostrUtil.hexToBytes(hex)));
            }
        }

        // Ref. pub keys
        arr = (ArrayValue) value.get("\"#p\"");
        if (arr != null) {
            for (var i = 0; i < arr.length(); i++) {
                String hex = arr.get(i).getValue().toString();
                refPublicKeyList.add(new PublicKey(NostrUtil.hexToBytes(hex)));
            }
        }

        //  Events        
        arr = (ArrayValue) value.get("\"ids\"");
        if (arr != null) {
            for (var i = 0; i < arr.length(); i++) {
                String eventId = arr.get(i).getValue().toString();
                try {
                    events.add(new ProxyEvent(eventId));
                } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
                    throw new NostrException(ex);
                }
            }
        }

        // Referenced Events        
        arr = (ArrayValue) value.get("\"#e\"");
        if (arr != null) {
            referencedEvents = new EventList();
            for (var i = 0; i < arr.length(); i++) {
                String eventId = arr.get(i).getValue().toString();
                try {
                    referencedEvents.add(new ProxyEvent(eventId));
                } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
                    throw new NostrException(ex);
                }
            }
        }

        // Kinds
        arr = (ArrayValue) value.get("\"kinds\"");
        if (arr != null) {
            for (var i = 0; i < arr.length(); i++) {
                var ikind = ((NumberValue) arr.get(i)).intValue();
                Kind kind = Kind.valueOf(ikind);
                kindList.add(kind);
            }
        }

        //Limit
        final NumberValue limitNumber = (NumberValue) value.get("\"limit\"");
        var limit = limitNumber != null ? limitNumber.intValue() : null;

        // Since
        final NumberValue sinceNumber = (NumberValue) value.get("\"since\"");
        var since = sinceNumber != null ? sinceNumber.longValue() : null;

        // Since
        final NumberValue untilNumber = (NumberValue) value.get("\"until\"");
        var until = untilNumber != null ? untilNumber.longValue() : null;

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
                    var s = attrArr.get(i).getValue().toString();
                    valueList.add(s);
                }
                var gtq = GenericTagQuery.builder().tagName(tagName).value(valueList).build();
                result.add(gtq);
            }
        }

        return result.size() == 0 ? null : result;
    }

}
